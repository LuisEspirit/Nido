package com.hotel.service;

import com.hotel.entity.Incidencia;
import java.util.List;
import java.util.Optional;

public interface IncidenciaService {
    List<Incidencia> findAll();
    Optional<Incidencia> findById(Integer id);
    Incidencia save(Incidencia incidencia);
    Incidencia update(Integer id, Incidencia incidencia);
    void deleteById(Integer id);
}