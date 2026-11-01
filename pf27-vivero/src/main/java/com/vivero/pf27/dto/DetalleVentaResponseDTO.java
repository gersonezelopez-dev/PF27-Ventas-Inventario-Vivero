package com.vivero.pf27.dto;

import java.math.BigDecimal;

public record DetalleVentaResponseDTO(
        Long productoId,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
