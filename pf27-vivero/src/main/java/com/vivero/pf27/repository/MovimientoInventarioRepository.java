package com.vivero.pf27.repository;

import com.vivero.pf27.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    // Consultar el historial de un producto
    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(
            Long productoId
    );
}
