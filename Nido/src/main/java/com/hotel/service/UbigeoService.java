package com.hotel.service;

import com.hotel.entity.Ubigeo;
import java.util.List;

public interface UbigeoService {
    List<Ubigeo> findAll();
    Ubigeo findById(Integer id);
    Ubigeo save(Ubigeo ubigeo);
    Ubigeo update(Integer id, Ubigeo ubigeo);
    void deleteById(Integer id);
}
