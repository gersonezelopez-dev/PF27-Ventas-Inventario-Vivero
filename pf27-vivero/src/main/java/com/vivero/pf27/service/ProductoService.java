package com.vivero.pf27.service;

import com.vivero.pf27.dto.ProductoRequestDTO;
import com.vivero.pf27.dto.ProductoResponseDTO;
import com.vivero.pf27.entity.Categoria;
import com.vivero.pf27.entity.Producto;
import com.vivero.pf27.repository.CategoriaRepository;
import com.vivero.pf27.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {
        return productoRepository.findAll().stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = obtenerProducto(id);
        return convertirDTO(producto);
    }

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

        return convertirDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO actualizar(
            Long id, ProductoRequestDTO dto) {

        Producto producto = obtenerProducto(id);
        String codigo = dto.getCodigo().trim();

        if (!producto.getCodigo().equals(codigo)
                && productoRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese codigo");
        }

        Categoria categoria = obtenerCategoria(dto.getCategoriaId());

        producto.setCodigo(codigo);
        producto.setNombre(dto.getNombre().trim());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        return convertirDTO(productoRepository.save(producto));
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtenerProducto(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorCategoria(Long categoriaId) {

        obtenerCategoria(categoriaId);

        return productoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarStockBajo(Integer limite) {

        if (limite == null || limite < 0) {
            throw new IllegalArgumentException(
                    "El limite no puede ser negativo");
        }

        return productoRepository.findByStockLessThanEqual(limite)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + id));
    }

    private Categoria obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoria no encontrada con ID: " + id));
    }

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
