package com.hotel.service;

import com.hotel.dto.DisponibilidadResponse;
import com.hotel.entity.Reserva;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaService {
    List<Reserva> findAll();
    Optional<Reserva> findById(Integer id);
    Reserva save(Reserva reserva);
    Reserva update(Integer id, Reserva reserva);
    void deleteById(Integer id);
    List<Reserva> listarPorAlojamiento(Integer idAlojamiento);
    Reserva cancelar(Integer id);
    DisponibilidadResponse consultarDisponibilidad(Integer idAlojamiento, LocalDateTime entrada, LocalDateTime salida);
}
