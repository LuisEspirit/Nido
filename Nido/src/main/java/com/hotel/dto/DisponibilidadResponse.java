package com.hotel.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Resultado de consultar si un alojamiento esta libre en un rango de fechas (US07). */
public record DisponibilidadResponse(
        Integer idAlojamiento,
        LocalDateTime entrada,
        LocalDateTime salida,
        boolean disponible,
        List<Conflicto> conflictos) {

    public record Conflicto(Integer idReserva, LocalDateTime entrada, LocalDateTime salida, String estado) {
    }
}
