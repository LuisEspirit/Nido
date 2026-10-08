package com.hotel.service.impl;

import com.hotel.entity.Pais;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.PaisRepository;
import com.hotel.service.PaisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaisServiceImpl implements PaisService {

    private final PaisRepository paisRepository;

    public PaisServiceImpl(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pais> findAll() {
        return paisRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Pais findById(Integer id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pais", id));
    }

    @Override
    @Transactional
    public Pais save(Pais pais) {
        return paisRepository.save(pais);
    }

    @Override
    @Transactional
    public Pais update(Integer id, Pais pais) {
        Pais existing = findById(id);
        existing.setIso(pais.getIso());
        existing.setNombre(pais.getNombre());
        return paisRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        paisRepository.delete(findById(id));
    }
}
