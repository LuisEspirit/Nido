package com.hotel.service.impl;

import com.hotel.entity.Catalogo;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.repository.CatalogoRepository;
import com.hotel.service.CatalogoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private final CatalogoRepository catalogoRepository;

    public CatalogoServiceImpl(CatalogoRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Catalogo> findAll() {
        return catalogoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Catalogo findById(Integer id) {
        return catalogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Catalogo", id));
    }

    @Override
    @Transactional
    public Catalogo save(Catalogo catalogo) {
        return catalogoRepository.save(catalogo);
    }

    @Override
    @Transactional
    public Catalogo update(Integer id, Catalogo catalogo) {
        Catalogo existing = findById(id);
        existing.setDescripcion(catalogo.getDescripcion());
        existing.setEstado(catalogo.getEstado());
        return catalogoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        catalogoRepository.delete(findById(id));
    }
}
