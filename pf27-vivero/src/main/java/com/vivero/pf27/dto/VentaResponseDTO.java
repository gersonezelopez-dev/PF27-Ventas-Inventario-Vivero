package com.vivero.pf27.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
        Long id,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        List<DetalleVentaResponseDTO> detalles
) {
}
