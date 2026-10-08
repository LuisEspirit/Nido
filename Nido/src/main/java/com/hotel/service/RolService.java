package com.hotel.service;

import com.hotel.entity.Rol;
import java.util.List;

public interface RolService {
    List<Rol> findAll();
    Rol findById(Integer id);
    Rol save(Rol rol);
    Rol update(Integer id, Rol rol);
    void deleteById(Integer id);
}
