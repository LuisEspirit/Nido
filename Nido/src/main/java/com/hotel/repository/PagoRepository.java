package com.hotel.repository;

import com.hotel.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReserva_IdReserva(Integer idReserva);

    /** Pagos registrados en el periodo, para el reporte de ingresos (US14). */
    @Query("""
            select p from Pago p
            where p.fecha between :desde and :hasta
              and upper(p.moneda) = upper(:moneda)
            order by p.fecha
            """)
    List<Pago> buscarEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                               @Param("moneda") String moneda);
}
