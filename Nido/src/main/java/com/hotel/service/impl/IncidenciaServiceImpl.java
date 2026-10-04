package com.hotel.service.impl;

import com.hotel.entity.Incidencia;
import com.hotel.repository.IncidenciaRepository;
import com.hotel.service.IncidenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> findAll() {
        return incidenciaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Incidencia> findById(Integer id) {
        return incidenciaRepository.findById(id);
    }

    @Override
    @Transactional
    public Incidencia save(Incidencia incidencia) {
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia update(Integer id, Incidencia incidencia) {
        return incidenciaRepository.findById(id).map(existing -> {
            existing.setCategoria(incidencia.getCategoria());
            existing.setPrioridad(incidencia.getPrioridad());
            existing.setDescripcion(incidencia.getDescripcion());
            existing.setEstado(incidencia.getEstado());
            existing.setAlojamiento(incidencia.getAlojamiento());
            existing.setReserva(incidencia.getReserva());
            existing.setServicio(incidencia.getServicio());
            return incidenciaRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Incidencia no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        incidenciaRepository.deleteById(id);
    }
}