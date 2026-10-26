package com.vivero.pf27.controller;

import com.vivero.pf27.dto.VentaRequestDTO;
import com.vivero.pf27.entity.Venta;
import com.vivero.pf27.service.VentaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // Consultar todas las ventas
    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    // Buscar venta por ID
    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ventaService.buscarPorId(id));
    }

    // Registrar una nueva venta
    @PostMapping
    public ResponseEntity<Venta> registrar(
            @Valid @RequestBody VentaRequestDTO dto) {

        Venta venta = ventaService.registrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(venta);
    }

    // Anular una venta
    @PatchMapping("/{id}/anular")
    public ResponseEntity<Venta> anular(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ventaService.anular(id));
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<Venta>> consultarPorFecha(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(
                ventaService.consultarPorFecha(fecha));
    }

    // Consultar total vendido durante un dia
    @GetMapping("/total-dia")
    public ResponseEntity<Map<String, Object>> totalDia(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        BigDecimal total =
                ventaService.totalVendidoPorFecha(fecha);

        return ResponseEntity.ok(Map.of(
                "fecha", fecha.toString(),
                "totalVendido", total
        ));
    }

}
