package com.hotel.service.impl;

import com.hotel.entity.Alojamiento;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.service.AlojamientoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlojamientoServiceImpl implements AlojamientoService {

    private final AlojamientoRepository alojamientoRepository;

    public AlojamientoServiceImpl(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alojamiento> findAll() {
        return alojamientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alojamiento> findById(Integer id) {
        return alojamientoRepository.findById(id);
    }

    @Override
    @Transactional
    public Alojamiento save(Alojamiento alojamiento) {
        return alojamientoRepository.save(alojamiento);
    }

    @Override
    @Transactional
    public Alojamiento update(Integer id, Alojamiento alojamiento) {
        return alojamientoRepository.findById(id).map(existing -> {
            existing.setNombre(alojamiento.getNombre());
            existing.setDireccion(alojamiento.getDireccion());
            existing.setLatitud(alojamiento.getLatitud());
            existing.setLongitud(alojamiento.getLongitud());
            existing.setCapacidad(alojamiento.getCapacidad());
            existing.setPrecioBase(alojamiento.getPrecioBase());
            existing.setEstado(alojamiento.getEstado());
            existing.setPropietario(alojamiento.getPropietario());
            existing.setUbigeo(alojamiento.getUbigeo());
            return alojamientoRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Alojamiento no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        alojamientoRepository.deleteById(id);
    }
}