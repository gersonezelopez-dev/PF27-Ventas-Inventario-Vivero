package com.vivero.pf27.dto;

import java.math.BigDecimal;

public class ProductoResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Boolean activo;
    private Long categoriaId;
    private String categoriaNombre;

    public ProductoResponseDTO() {
    }

    public ProductoResponseDTO(Long id, String codigo, String nombre,
                               BigDecimal precio, Integer stock, Boolean activo,
                               Long categoriaId, String categoriaNombre) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.activo = activo;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public Boolean getActivo() {
        return activo;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }
}
