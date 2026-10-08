package com.hotel.service.impl;

import com.hotel.entity.Ubigeo;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.UbigeoRepository;
import com.hotel.service.UbigeoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UbigeoServiceImpl implements UbigeoService {

    private final UbigeoRepository ubigeoRepository;

    public UbigeoServiceImpl(UbigeoRepository ubigeoRepository) {
        this.ubigeoRepository = ubigeoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ubigeo> findAll() {
        return ubigeoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Ubigeo findById(Integer id) {
        return ubigeoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubigeo", id));
    }

    @Override
    @Transactional
    public Ubigeo save(Ubigeo ubigeo) {
        return ubigeoRepository.save(ubigeo);
    }

    @Override
    @Transactional
    public Ubigeo update(Integer id, Ubigeo ubigeo) {
        Ubigeo existing = findById(id);
        existing.setDepartamento(ubigeo.getDepartamento());
        existing.setProvincia(ubigeo.getProvincia());
        existing.setDistrito(ubigeo.getDistrito());
        return ubigeoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        ubigeoRepository.delete(findById(id));
    }
}
