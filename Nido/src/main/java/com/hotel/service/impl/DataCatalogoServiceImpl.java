package com.hotel.service.impl;

import com.hotel.entity.DataCatalogo;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.DataCatalogoRepository;
import com.hotel.service.DataCatalogoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataCatalogoServiceImpl implements DataCatalogoService {

    private final DataCatalogoRepository dataCatalogoRepository;

    public DataCatalogoServiceImpl(DataCatalogoRepository dataCatalogoRepository) {
        this.dataCatalogoRepository = dataCatalogoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataCatalogo> findAll() {
        return dataCatalogoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DataCatalogo findById(Integer id) {
        return dataCatalogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dato de catalogo", id));
    }

    @Override
    @Transactional
    public DataCatalogo save(DataCatalogo dataCatalogo) {
        return dataCatalogoRepository.save(dataCatalogo);
    }

    @Override
    @Transactional
    public DataCatalogo update(Integer id, DataCatalogo dataCatalogo) {
        DataCatalogo existing = findById(id);
        existing.setDescripcion(dataCatalogo.getDescripcion());
        existing.setEstado(dataCatalogo.getEstado());
        existing.setCatalogo(dataCatalogo.getCatalogo());
        return dataCatalogoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        dataCatalogoRepository.delete(findById(id));
    }
}
