package com.vivero.pf27.dto;

import java.time.LocalDateTime;

public record MovimientoInventarioResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        String tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        String motivo,
        LocalDateTime fecha
) {
}
