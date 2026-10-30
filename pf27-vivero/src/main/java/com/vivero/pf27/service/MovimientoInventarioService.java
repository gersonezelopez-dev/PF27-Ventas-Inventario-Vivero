package com.vivero.pf27.service;

import com.vivero.pf27.dto.MovimientoInventarioRequestDTO;
import com.vivero.pf27.dto.MovimientoInventarioResponseDTO;
import com.vivero.pf27.entity.MovimientoInventario;
import com.vivero.pf27.entity.Producto;
import com.vivero.pf27.exception.RecursoNoEncontradoException;
import com.vivero.pf27.repository.MovimientoInventarioRepository;
import com.vivero.pf27.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepository,
            ProductoRepository productoRepository) {

        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    // Registrar una entrada o salida justificada
    @Transactional
    public MovimientoInventarioResponseDTO registrar(
            MovimientoInventarioRequestDTO dto) {

        if (dto == null || dto.getProductoId() == null
                || dto.getProductoId() <= 0
                || dto.getCantidad() == null
                || dto.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "Producto o cantidad invalida");
        }

        if (dto.getMotivo() == null
                || dto.getMotivo().isBlank()
                || dto.getMotivo().length() > 255) {

            throw new IllegalArgumentException(
                    "Debe indicar un motivo valido");
        }

        String tipo = dto.getTipo();

        if (!"ENTRADA".equals(tipo) && !"SALIDA".equals(tipo)) {
            throw new IllegalArgumentException(
                    "El tipo debe ser ENTRADA o SALIDA");
        }

        // Bloquear producto durante el movimiento
        Producto producto = productoRepository
                .buscarParaVenta(dto.getProductoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto no encontrado: "
                                        + dto.getProductoId()));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new IllegalArgumentException(
                    "No se permiten movimientos sobre productos desactivados");
        }

        if (producto.getStock() == null) {
            throw new IllegalStateException(
                    "El producto tiene un stock invalido");
        }

        int stockAnterior = producto.getStock();
        int cantidad = dto.getCantidad();
        int stockNuevo;

        if ("ENTRADA".equals(tipo)) {
            try {
                stockNuevo = Math.addExact(stockAnterior, cantidad);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "La cantidad supera el stock permitido");
            }
        } else {
            if (stockAnterior < cantidad) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para realizar la salida");
            }

            stockNuevo = stockAnterior - cantidad;
        }

        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(dto.getMotivo().trim());

        MovimientoInventario guardado =
                movimientoRepository.save(movimiento);

        return convertirDTO(guardado);
    }

    // Consultar movimientos de un producto
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponseDTO> historial(
            Long productoId) {

        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del producto es invalido");
        }

        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException(
                    "Producto no encontrado: " + productoId);
        }

        return movimientoRepository
                .findByProductoIdOrderByFechaDesc(productoId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Convertir entidad a DTO de respuesta
    private MovimientoInventarioResponseDTO convertirDTO(
            MovimientoInventario movimiento) {

        return new MovimientoInventarioResponseDTO(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getMotivo(),
                movimiento.getFecha()
        );
    }
}
