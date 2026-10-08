package com.hotel.service;

import com.hotel.entity.Catalogo;
import java.util.List;

public interface CatalogoService {
    List<Catalogo> findAll();
    Catalogo findById(Integer id);
    Catalogo save(Catalogo catalogo);
    Catalogo update(Integer id, Catalogo catalogo);
    void deleteById(Integer id);
}
