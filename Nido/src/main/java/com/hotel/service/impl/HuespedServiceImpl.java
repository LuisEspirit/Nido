package com.hotel.service.impl;

import com.hotel.entity.Huesped;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.HuespedRepository;
import com.hotel.service.HuespedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HuespedServiceImpl implements HuespedService {

    private final HuespedRepository huespedRepository;

    public HuespedServiceImpl(HuespedRepository huespedRepository) {
        this.huespedRepository = huespedRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Huesped> findAll() {
        return huespedRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Huesped> findById(Integer id) {
        return huespedRepository.findById(id);
    }

    @Override
    @Transactional
    public Huesped save(Huesped huesped) {
        huesped.setIdHuesped(null);
        return huespedRepository.save(huesped);
    }

    @Override
    @Transactional
    public Huesped update(Integer id, Huesped huesped) {
        return huespedRepository.findById(id).map(existing -> {
            existing.setNombres(huesped.getNombres());
            existing.setApellidos(huesped.getApellidos());
            existing.setCorreo(huesped.getCorreo());
            existing.setTelefono(huesped.getTelefono());
            existing.setConsentimiento(huesped.getConsentimiento());
            return huespedRepository.save(existing);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Huesped", id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        huespedRepository.delete(huespedRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped", id)));
    }
}