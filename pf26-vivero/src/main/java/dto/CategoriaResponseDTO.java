package com.vivero.pf26.dto;

public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    public CategoriaResponseDTO() {
    }

    public CategoriaResponseDTO(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

