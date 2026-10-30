package com.vivero.pf27.controller;

import com.vivero.pf27.dto.MovimientoInventarioRequestDTO;
import com.vivero.pf27.dto.MovimientoInventarioResponseDTO;
import com.vivero.pf27.service.MovimientoInventarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(
            MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    // Registrar entrada o salida de inventario
    @PostMapping
    public ResponseEntity<MovimientoInventarioResponseDTO> registrar(
            @Valid @RequestBody MovimientoInventarioRequestDTO dto) {

        MovimientoInventarioResponseDTO movimiento =
                movimientoService.registrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimiento);
    }

    // Consultar movimientos de un producto
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> historial(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                movimientoService.historial(id)
        );
    }
}
