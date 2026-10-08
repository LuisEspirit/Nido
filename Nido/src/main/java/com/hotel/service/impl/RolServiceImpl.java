package com.hotel.service.impl;

import com.hotel.entity.Rol;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.RolRepository;
import com.hotel.service.RolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Rol findById(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", id));
    }

    @Override
    @Transactional
    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public Rol update(Integer id, Rol rol) {
        Rol existing = findById(id);
        existing.setNombre(rol.getNombre());
        existing.setEstado(rol.getEstado());
        existing.setOpciones(rol.getOpciones());
        return rolRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        rolRepository.delete(findById(id));
    }
}
