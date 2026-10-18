package com.vivero.pf26.repository;

import com.vivero.pf26.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByStockLessThanEqual(Integer stock);

    List<Producto> findByActivoTrue();

    boolean existsByCodigo(String codigo);
}
