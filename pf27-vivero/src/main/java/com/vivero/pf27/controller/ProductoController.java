package com.vivero.pf27.controller;

import com.vivero.pf27.dto.ProductoRequestDTO;
import com.vivero.pf27.dto.ProductoResponseDTO;
import com.vivero.pf27.service.ProductoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Consultar todos los productos
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    // Buscar producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    // Registrar un producto nuevo
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> registrar(
            @Valid @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO respuesta = productoService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Actualizar un producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    // Desactivar un producto
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Consultar productos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorCategoria(
            @PathVariable Long categoriaId) {
        return ResponseEntity.ok(
                productoService.buscarPorCategoria(categoriaId));
    }

    // Consultar productos con stock bajo
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponseDTO>> buscarStockBajo(
            @RequestParam(defaultValue = "5") Integer limite) {
        return ResponseEntity.ok(
                productoService.buscarStockBajo(limite));
    }
}
