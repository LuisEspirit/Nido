package com.hotel.service.impl;

import com.hotel.entity.Reserva;
import com.hotel.repository.ReservaRepository;
import com.hotel.service.ReservaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> findById(Integer id) {
        return reservaRepository.findById(id);
    }

    @Override
    @Transactional
    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva update(Integer id, Reserva reserva) {
        return reservaRepository.findById(id).map(existing -> {
            existing.setEntrada(reserva.getEntrada());
            existing.setSalida(reserva.getSalida());
            existing.setCanal(reserva.getCanal());
            existing.setPrecio(reserva.getPrecio());
            existing.setMoneda(reserva.getMoneda());
            existing.setEstado(reserva.getEstado());
            existing.setAlojamiento(reserva.getAlojamiento());
            existing.setHuesped(reserva.getHuesped());
            return reservaRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        reservaRepository.deleteById(id);
    }
}