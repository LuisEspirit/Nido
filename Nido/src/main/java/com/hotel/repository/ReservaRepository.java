package com.hotel.repository;

import com.hotel.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByAlojamiento_IdAlojamientoOrderByEntradaAsc(Integer idAlojamiento);

    /**
     * Reservas activas (no canceladas) del alojamiento que se cruzan con el rango [entrada, salida).
     * Dos reservas se solapan si una empieza antes de que termine la otra (US07).
     * idExcluir permite ignorar la propia reserva al actualizarla.
     */
    @Query("""
            select r from Reserva r
            where r.alojamiento.idAlojamiento = :idAlojamiento
              and (r.estado is null or upper(r.estado) <> 'CANCELADA')
              and r.entrada < :salida
              and r.salida > :entrada
              and (:idExcluir is null or r.idReserva <> :idExcluir)
            order by r.entrada
            """)
    List<Reserva> buscarSolapadas(@Param("idAlojamiento") Integer idAlojamiento,
                                  @Param("entrada") LocalDateTime entrada,
                                  @Param("salida") LocalDateTime salida,
                                  @Param("idExcluir") Integer idExcluir);

    /** Reservas no canceladas que ocupan algun dia del periodo (calendario US08 y ocupacion US14). */
    @Query("""
            select r from Reserva r
            where (r.estado is null or upper(r.estado) <> 'CANCELADA')
              and r.entrada < :hasta
              and r.salida > :desde
            order by r.entrada
            """)
    List<Reserva> buscarEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
