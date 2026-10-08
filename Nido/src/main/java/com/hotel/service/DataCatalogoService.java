package com.hotel.service;

import com.hotel.entity.DataCatalogo;
import java.util.List;

public interface DataCatalogoService {
    List<DataCatalogo> findAll();
    DataCatalogo findById(Integer id);
    DataCatalogo save(DataCatalogo dataCatalogo);
    DataCatalogo update(Integer id, DataCatalogo dataCatalogo);
    void deleteById(Integer id);
}
