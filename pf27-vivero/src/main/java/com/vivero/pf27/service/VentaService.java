package com.vivero.pf27.service;

import com.vivero.pf27.dto.DetalleVentaRequestDTO;
import com.vivero.pf27.dto.VentaRequestDTO;
import com.vivero.pf27.entity.DetalleVenta;
import com.vivero.pf27.entity.Producto;
import com.vivero.pf27.entity.Venta;
import com.vivero.pf27.repository.DetalleVentaRepository;
import com.vivero.pf27.repository.ProductoRepository;
import com.vivero.pf27.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleRepository;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            DetalleVentaRepository detalleRepository) {

        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.detalleRepository = detalleRepository;
    }

    // Registrar venta y descontar inventario
    @Transactional
    public Venta registrar(VentaRequestDTO dto) {

        if (dto == null || dto.getDetalles() == null
                || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "La venta debe contener productos");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();

        // Agrupar cantidades de productos repetidos
        for (DetalleVentaRequestDTO item : dto.getDetalles()) {

            if (item == null || item.getProductoId() == null
                    || item.getCantidad() == null
                    || item.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Producto o cantidad invalida");
            }

            cantidades.merge(
                    item.getProductoId(),
                    item.getCantidad(),
                    Math::addExact
            );
        }

        Map<Long, Producto> productos = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;

        // Verificar productos y existencias
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaVenta(item.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado: " + item.getKey()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new IllegalArgumentException(
                        "El producto esta desactivado");
            }

            if (producto.getStock() == null
                    || producto.getStock() < item.getValue()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente: " + producto.getNombre());
            }

            if (producto.getPrecio() == null
                    || producto.getPrecio().signum() < 0) {
                throw new IllegalArgumentException(
                        "Precio invalido del producto");
            }

            productos.put(item.getKey(), producto);

            BigDecimal subtotal = producto.getPrecio().multiply(
                    BigDecimal.valueOf(item.getValue()));

            total = total.add(subtotal);
        }

        // Crear venta
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("REGISTRADA");
        venta.setTotal(total);

        venta = ventaRepository.save(venta);

        // Guardar detalles y descontar existencias
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productos.get(item.getKey());
            int cantidad = item.getValue();

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());

            BigDecimal subtotal = producto.getPrecio().multiply(
                    BigDecimal.valueOf(cantidad));

            detalle.setSubtotal(subtotal);
            detalleRepository.save(detalle);
        }

        return venta;
    }

    // Consultar todas las ventas
    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAll();
    }

    // Buscar venta por ID
    @Transactional(readOnly = true)
    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Venta no encontrada: " + id));
    }

    // Anular venta y devolver existencias
    @Transactional
    public Venta anular(Long id) {

        Venta venta = ventaRepository.buscarParaAnular(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Venta no encontrada: " + id));

        if (!"REGISTRADA".equals(venta.getEstado())) {
            throw new IllegalArgumentException(
                    "La venta ya fue anulada");
        }

        List<DetalleVenta> detalles =
                detalleRepository.findByVentaId(id);

        if (detalles.isEmpty()) {
            throw new IllegalStateException(
                    "La venta no tiene detalles");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();

        for (DetalleVenta detalle : detalles) {
            cantidades.merge(
                    detalle.getProducto().getId(),
                    detalle.getCantidad(),
                    Math::addExact
            );
        }

        // Restituir inventario
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaVenta(item.getKey())
                    .orElseThrow(() -> new IllegalStateException(
                            "Producto no encontrado"));

            producto.setStock(
                    Math.addExact(producto.getStock(), item.getValue()));

            productoRepository.save(producto);
        }


        venta.setEstado("ANULADA");

        return ventaRepository.save(venta);
    }

    // Consultar ventas por fecha
    @Transactional(readOnly = true)
    public List<Venta> consultarPorFecha(LocalDate fecha) {

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        return ventaRepository.buscarPorRangoFechas(inicio, fin);
    }
    @Transactional(readOnly = true)
    public BigDecimal totalVendidoPorFecha(LocalDate fecha) {

        List<Venta> ventas = consultarPorFecha(fecha);

        return ventas.stream()
                .filter(venta ->
                        "REGISTRADA".equals(venta.getEstado()))
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
