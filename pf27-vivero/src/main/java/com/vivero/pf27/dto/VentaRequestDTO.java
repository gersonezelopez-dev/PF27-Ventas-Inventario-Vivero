package com.vivero.pf27.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class VentaRequestDTO {

    @NotEmpty(message = "La venta debe tener productos")
    private List<@NotNull @Valid DetalleVentaRequestDTO> detalles;

    public VentaRequestDTO() {
    }

    public List<DetalleVentaRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaRequestDTO> detalles) {
        this.detalles = detalles;
    }
}
