package com.hotel.service;

import com.hotel.entity.Opcion;
import java.util.List;

public interface OpcionService {
    List<Opcion> findAll();
    Opcion findById(Integer id);
    Opcion save(Opcion opcion);
    Opcion update(Integer id, Opcion opcion);
    void deleteById(Integer id);
}
