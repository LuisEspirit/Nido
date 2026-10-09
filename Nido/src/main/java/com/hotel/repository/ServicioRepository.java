package com.hotel.repository;

import com.hotel.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    /** Servicios asignados a un personal operativo. */
    List<Servicio> findByPersonal_IdusuarioOrderByInicioAsc(Integer idUsuario);

    /** Servicios que empiezan dentro del periodo (calendario US08). */
    List<Servicio> findByInicioBetweenOrderByInicioAsc(LocalDateTime desde, LocalDateTime hasta);

    @Query("""
            select s.estado, count(s)
            from Servicio s
            where s.inicio between :desde and :hasta
            group by s.estado
            """)
    List<Object[]> contarPorEstado(@Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta);

    /** Servicios del periodo que tienen al menos una evidencia fotografica (US12, US15). */
    @Query("""
            select count(s)
            from Servicio s
            where s.inicio between :desde and :hasta
              and exists (select e from Evidencia e where e.servicio = s)
            """)
    long contarConEvidenciaCompleta(@Param("desde") LocalDateTime desde,
                                    @Param("hasta") LocalDateTime hasta);

    @Query("""
            select s
            from Servicio s
            where s.inicio between :desde and :hasta
              and s.fin is not null
              and s.fin < :ahora
              and (s.estado is null or upper(s.estado) not in :finalizados)
            order by s.fin
            """)
    List<Servicio> buscarAtrasados(@Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta,
                                   @Param("ahora") LocalDateTime ahora,
                                   @Param("finalizados") List<String> finalizados);
}
