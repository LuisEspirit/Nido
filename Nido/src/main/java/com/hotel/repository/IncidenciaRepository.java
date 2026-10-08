package com.hotel.repository;

import com.hotel.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    @Query("""
            select i.prioridad, count(i)
            from Incidencia i
            left join i.servicio s
            left join i.reserva r
            where (s.inicio between :desde and :hasta)
               or (s is null and r.entrada between :desde and :hasta)
            group by i.prioridad
            """)
    List<Object[]> contarPorPrioridad(@Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);
}