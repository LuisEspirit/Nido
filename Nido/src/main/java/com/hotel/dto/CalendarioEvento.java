package com.hotel.dto;

import java.time.LocalDateTime;

/** Un evento del calendario (US08): una reserva o un servicio programado. */
public record CalendarioEvento(
        String tipo,
        Integer id,
        Integer idAlojamiento,
        String alojamiento,
        LocalDateTime inicio,
        LocalDateTime fin,
        String estado,
        String detalle) {
}
