package com.vivero.pf27.service;

import com.vivero.pf27.dto.DetalleVentaRequestDTO;
import com.vivero.pf27.dto.VentaRequestDTO;
import com.vivero.pf27.entity.DetalleVenta;
import com.vivero.pf27.entity.MovimientoInventario;
import com.vivero.pf27.entity.Producto;
import com.vivero.pf27.entity.Venta;
import com.vivero.pf27.exception.RecursoNoEncontradoException;
import com.vivero.pf27.repository.DetalleVentaRepository;
import com.vivero.pf27.repository.MovimientoInventarioRepository;
import com.vivero.pf27.repository.ProductoRepository;
import com.vivero.pf27.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.vivero.pf27.dto.VentaResponseDTO;
import com.vivero.pf27.dto.DetalleVentaResponseDTO;


@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            DetalleVentaRepository detalleRepository,
            MovimientoInventarioRepository movimientoRepository) {

        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.detalleRepository = detalleRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // Registrar una venta y descontar inventario
    @Transactional
    public Venta registrar(VentaRequestDTO dto) {

        if (dto == null || dto.getDetalles() == null
                || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "La venta debe contener productos");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();

        // Agrupar productos repetidos
        for (DetalleVentaRequestDTO item : dto.getDetalles()) {

            if (item == null || item.getProductoId() == null
                    || item.getProductoId() <= 0
                    || item.getCantidad() == null
                    || item.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Producto o cantidad invalida");
            }

            try {
                cantidades.merge(
                        item.getProductoId(),
                        item.getCantidad(),
                        Math::addExact
                );
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "Cantidad total demasiado grande");
            }
        }

        Map<Long, Producto> productos = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;

        // Verificar existencias y precios
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaVenta(item.getKey())
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Producto no encontrado: "
                                            + item.getKey()));

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
                    || producto.getPrecio().signum() <= 0) {
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

        // Descontar existencias y registrar movimientos
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productos.get(item.getKey());
            int cantidad = item.getValue();

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - cantidad;

            producto.setStock(stockNuevo);
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

            // Guardar salida por venta
            registrarMovimiento(
                    producto,
                    "SALIDA",
                    cantidad,
                    stockAnterior,
                    stockNuevo,
                    "Salida por venta #" + venta.getId()
            );
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
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Venta no encontrada: " + id));
    }

    // Anular venta y devolver existencias
    @Transactional
    public Venta anular(Long id) {

        Venta venta = ventaRepository.buscarParaAnular(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Venta no encontrada: " + id));

        if (!"REGISTRADA".equals(venta.getEstado())) {
            throw new IllegalStateException(
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

            if (detalle.getProducto() == null
                    || detalle.getProducto().getId() == null
                    || detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {
                throw new IllegalStateException(
                        "Detalle de venta invalido");
            }

            try {
                cantidades.merge(
                        detalle.getProducto().getId(),
                        detalle.getCantidad(),
                        Math::addExact
                );
            } catch (ArithmeticException ex) {
                throw new IllegalStateException(
                        "Cantidad de anulacion invalida");
            }
        }

        // Restituir inventario y registrar movimientos
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaVenta(item.getKey())
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Producto no encontrado: "
                                            + item.getKey()));

            int stockAnterior = producto.getStock();
            int stockNuevo;

            try {
                stockNuevo = Math.addExact(
                        stockAnterior,
                        item.getValue()
                );
            } catch (ArithmeticException ex) {
                throw new IllegalStateException(
                        "No se puede restituir el inventario");
            }

            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            // Guardar entrada por anulacion
            registrarMovimiento(
                    producto,
                    "ENTRADA",
                    item.getValue(),
                    stockAnterior,
                    stockNuevo,
                    "Entrada por anulacion de venta #" + venta.getId()
            );
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

    // Calcular total vendido por fecha, excluyendo anuladas
    @Transactional(readOnly = true)
    public BigDecimal totalVendidoPorFecha(LocalDate fecha) {

        return consultarPorFecha(fecha)
                .stream()
                .filter(venta ->
                        "REGISTRADA".equals(venta.getEstado()))
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Guardar un movimiento de inventario
    // Se ejecuta dentro de la transaccion de venta/anulacion
    private void registrarMovimiento(
            Producto producto,
            String tipo,
            int cantidad,
            int stockAnterior,
            int stockNuevo,
            String motivo) {

        MovimientoInventario movimiento = new MovimientoInventario();

        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(motivo);

        movimientoRepository.save(movimiento);
    }


    // Convertir una venta en DTO con sus productos
    @Transactional(readOnly = true)
    public VentaResponseDTO convertirDTO(Venta venta) {

        List<DetalleVentaResponseDTO> detalles =
                detalleRepository.findByVentaId(venta.getId())
                        .stream()
                        .map(detalle -> new DetalleVentaResponseDTO(
                                detalle.getProducto().getId(),
                                detalle.getProducto().getNombre(),
                                detalle.getCantidad(),
                                detalle.getPrecioUnitario(),
                                detalle.getSubtotal()
                        ))
                        .toList();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getEstado(),
                venta.getTotal(),
                detalles
        );
    }
}


