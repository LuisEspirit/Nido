package com.hotel.service;

import com.hotel.entity.Huesped;
import java.util.List;
import java.util.Optional;

public interface HuespedService {
    List<Huesped> findAll();
    Optional<Huesped> findById(Integer id);
    Huesped save(Huesped huesped);
    Huesped update(Integer id, Huesped huesped);
    void deleteById(Integer id);
}