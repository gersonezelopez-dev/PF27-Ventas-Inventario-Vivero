
package com.vivero.pf27.service;

import com.vivero.pf27.dto.ProductoRequestDTO;
import com.vivero.pf27.dto.ProductoResponseDTO;
import com.vivero.pf27.entity.Categoria;
import com.vivero.pf27.entity.Producto;
import com.vivero.pf27.entity.MovimientoInventario;
import com.vivero.pf27.repository.CategoriaRepository;
import com.vivero.pf27.repository.ProductoRepository;
import com.vivero.pf27.repository.MovimientoInventarioRepository;
import com.vivero.pf27.exception.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MovimientoInventarioRepository movimientoRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // Consultar todos los productos
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {

        return productoRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Buscar producto por ID
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {

        Producto producto = obtenerProducto(id);

        return convertirDTO(producto);
    }

    // Registrar un producto y su movimiento de stock inicial
    @Transactional
    public ProductoResponseDTO registrar(ProductoRequestDTO dto) {

        String codigo = dto.getCodigo().trim();

        if (productoRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese codigo");
        }

        Categoria categoria = obtenerCategoria(dto.getCategoriaId());

        Producto producto = new Producto();

        producto.setCodigo(codigo);
        producto.setNombre(dto.getNombre().trim());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setActivo(true);
        producto.setCategoria(categoria);

        Producto productoGuardado = productoRepository.save(producto);

        // Registrar la entrada inicial para conservar trazabilidad
        if (productoGuardado.getStock() > 0) {

            MovimientoInventario movimiento = new MovimientoInventario();

            movimiento.setProducto(productoGuardado);
            movimiento.setTipo("ENTRADA");
            movimiento.setCantidad(productoGuardado.getStock());
            movimiento.setStockAnterior(0);
            movimiento.setStockNuevo(productoGuardado.getStock());
            movimiento.setMotivo("Stock inicial al registrar producto");

            movimientoRepository.save(movimiento);
        }

        return convertirDTO(productoGuardado);
    }

    // Actualizar informacion sin cambiar stock
    @Transactional
    public ProductoResponseDTO actualizar(
            Long id, ProductoRequestDTO dto) {

        Producto producto = obtenerProducto(id);
        String codigo = dto.getCodigo().trim();

        // Validar que el codigo no este duplicado
        if (!producto.getCodigo().equals(codigo)
                && productoRepository.existsByCodigo(codigo)) {

            throw new IllegalArgumentException(
                    "Ya existe un producto con ese codigo");
        }

        Categoria categoria = obtenerCategoria(dto.getCategoriaId());

        // Prohibir modificaciones directas al inventario
        if (!producto.getStock().equals(dto.getStock())) {

            throw new IllegalArgumentException(
                    "El stock no puede modificarse desde " +
                            "la actualizacion del producto. " +
                            "Utilice un movimiento de inventario.");
        }

        producto.setCodigo(codigo);
        producto.setNombre(dto.getNombre().trim());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(categoria);

        return convertirDTO(productoRepository.save(producto));
    }

    // Desactivar un producto
    @Transactional
    public void desactivar(Long id) {

        Producto producto = obtenerProducto(id);

        producto.setActivo(false);

        productoRepository.save(producto);
    }

    // Eliminacion logica del producto
    @Transactional
    public void eliminar(Long id) {

        desactivar(id);
    }

    // Consultar productos por categoria
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorCategoria(
            Long categoriaId) {

        obtenerCategoria(categoriaId);

        return productoRepository
                .findByCategoriaId(categoriaId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Consultar productos con stock bajo
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarStockBajo(
            Integer limite) {

        if (limite == null || limite < 0) {

            throw new IllegalArgumentException(
                    "El limite no puede ser negativo");
        }

        return productoRepository
                .findByStockLessThanEqual(limite)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Obtener un producto existente
    private Producto obtenerProducto(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto no encontrado con ID: " + id
                        )
                );
    }

    // Obtener una categoria existente
    private Categoria obtenerCategoria(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Categoria no encontrada con ID: " + id
                        )
                );
    }

    // Convertir entidad Producto a DTO
    private ProductoResponseDTO convertirDTO(Producto producto) {

        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getActivo(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}
