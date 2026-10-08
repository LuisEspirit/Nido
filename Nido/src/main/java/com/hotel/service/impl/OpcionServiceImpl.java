package com.hotel.service.impl;

import com.hotel.entity.Opcion;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.OpcionRepository;
import com.hotel.service.OpcionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpcionServiceImpl implements OpcionService {

    private final OpcionRepository opcionRepository;

    public OpcionServiceImpl(OpcionRepository opcionRepository) {
        this.opcionRepository = opcionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Opcion> findAll() {
        return opcionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Opcion findById(Integer id) {
        return opcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Opcion", id));
    }

    @Override
    @Transactional
    public Opcion save(Opcion opcion) {
        return opcionRepository.save(opcion);
    }

    @Override
    @Transactional
    public Opcion update(Integer id, Opcion opcion) {
        Opcion existing = findById(id);
        existing.setNombre(opcion.getNombre());
        existing.setEstado(opcion.getEstado());
        existing.setRuta(opcion.getRuta());
        existing.setTipo(opcion.getTipo());
        return opcionRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        opcionRepository.delete(findById(id));
    }
}
