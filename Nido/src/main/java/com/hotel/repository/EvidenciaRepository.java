package com.hotel.repository;

import com.hotel.entity.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Integer> {

    List<Evidencia> findByServicio_IdServicioOrderByFechaAsc(Integer idServicio);
}
