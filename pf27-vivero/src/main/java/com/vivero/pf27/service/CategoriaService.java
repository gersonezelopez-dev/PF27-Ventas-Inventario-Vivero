
package com.vivero.pf27.service;

import com.vivero.pf27.dto.CategoriaRequestDTO;
import com.vivero.pf27.dto.CategoriaResponseDTO;
import com.vivero.pf27.entity.Categoria;
import com.vivero.pf27.repository.CategoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Consultar todas las categorias
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Buscar categoria por ID
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoria no encontrada con ID: " + id));

        return convertirDTO(categoria);
    }

    // Registrar una nueva categoria
    @Transactional
    public CategoriaResponseDTO registrar(CategoriaRequestDTO dto) {

        String nombre = dto.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException(
                    "Ya existe una categoria con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());

        return convertirDTO(categoriaRepository.save(categoria));
    }

    // Actualizar una categoria
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoria no encontrada con ID: " + id));

        String nombre = dto.getNombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Ya existe una categoria con ese nombre");
                    }
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());

        return convertirDTO(categoriaRepository.save(categoria));
    }

    // Eliminar una categoria
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoria no encontrada con ID: " + id));

        categoriaRepository.delete(categoria);
    }

    // Convertir entidad a DTO
    private CategoriaResponseDTO convertirDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
