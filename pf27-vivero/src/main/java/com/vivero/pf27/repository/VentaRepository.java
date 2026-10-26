package com.vivero.pf27.repository;

import com.vivero.pf27.entity.Venta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Venta v WHERE v.id = :id")
    Optional<Venta> buscarParaAnular(@Param("id") Long id);


    @Query("""
       SELECT v FROM Venta v
       WHERE v.fecha >= :inicio
       AND v.fecha < :fin
       ORDER BY v.fecha ASC
       """)
    List<Venta> buscarPorRangoFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

}
