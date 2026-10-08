package com.hotel.repository;

import com.hotel.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    @Query("""
            select i from Incidencia i
            where (:estado is null or upper(i.estado) = upper(:estado))
              and (:prioridad is null or upper(i.prioridad) = upper(:prioridad))
              and (:idAlojamiento is null or i.alojamiento.idAlojamiento = :idAlojamiento)
            order by i.idIncidencia desc
            """)
    List<Incidencia> buscar(@Param("estado") String estado, @Param("prioridad") String prioridad,
                            @Param("idAlojamiento") Integer idAlojamiento);

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
