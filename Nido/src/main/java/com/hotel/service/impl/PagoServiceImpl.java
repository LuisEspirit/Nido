package com.hotel.service.impl;

import com.hotel.entity.Pago;
import com.hotel.repository.PagoRepository;
import com.hotel.service.PagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Integer id) {
        return pagoRepository.findById(id);
    }

    @Override
    @Transactional
    public Pago save(Pago pago) {
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago update(Integer id, Pago pago) {
        return pagoRepository.findById(id).map(existing -> {
            existing.setTipo(pago.getTipo());
            existing.setImporte(pago.getImporte());
            existing.setMoneda(pago.getMoneda());
            existing.setFecha(pago.getFecha());
            existing.setEstado(pago.getEstado());
            existing.setReserva(pago.getReserva());
            return pagoRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        pagoRepository.deleteById(id);
    }
}