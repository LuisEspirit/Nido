package com.hotel.service;

import com.hotel.entity.Alojamiento;
import java.util.List;
import java.util.Optional;

public interface AlojamientoService {
    List<Alojamiento> findAll();
    Optional<Alojamiento> findById(Integer id);
    Alojamiento save(Alojamiento alojamiento);
    Alojamiento update(Integer id, Alojamiento alojamiento);
    void deleteById(Integer id);
}