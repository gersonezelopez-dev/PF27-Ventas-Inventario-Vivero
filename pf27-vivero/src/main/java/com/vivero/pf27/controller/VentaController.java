
package com.vivero.pf27.controller;

import com.vivero.pf27.dto.VentaRequestDTO;
import com.vivero.pf27.dto.VentaResponseDTO;
import com.vivero.pf27.entity.Venta;
import com.vivero.pf27.service.VentaService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // Consultar todas las ventas con sus detalles
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listar() {

        List<VentaResponseDTO> ventas =
                ventaService.listar().stream()
                        .map(ventaService::convertirDTO)
                        .toList();

        return ResponseEntity.ok(ventas);
    }

    // Consultar venta por ID
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarPorId(
            @PathVariable Long id) {

        Venta venta = ventaService.buscarPorId(id);

        return ResponseEntity.ok(
                ventaService.convertirDTO(venta)
        );
    }

    // Registrar una venta
    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrar(
            @Valid @RequestBody VentaRequestDTO dto) {

        Venta venta = ventaService.registrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ventaService.convertirDTO(venta));
    }

    // Anular una venta
    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anular(
            @PathVariable Long id) {

        Venta venta = ventaService.anular(id);

        return ResponseEntity.ok(
                ventaService.convertirDTO(venta)
        );
    }

    // Consultar ventas por fecha
    @GetMapping("/fecha")
    public ResponseEntity<List<VentaResponseDTO>> consultarPorFecha(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        List<VentaResponseDTO> ventas =
                ventaService.consultarPorFecha(fecha)
                        .stream()
                        .map(ventaService::convertirDTO)
                        .toList();

        return ResponseEntity.ok(ventas);
    }

    // Consultar el total vendido por fecha
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
