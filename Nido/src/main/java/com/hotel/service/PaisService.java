package com.hotel.service;

import com.hotel.entity.Pais;
import java.util.List;

public interface PaisService {
    List<Pais> findAll();
    Pais findById(Integer id);
    Pais save(Pais pais);
    Pais update(Integer id, Pais pais);
    void deleteById(Integer id);
}
