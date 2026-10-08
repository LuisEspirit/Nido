package com.hotel.dto;

import java.time.LocalDate;
import java.util.List;

/** Noches ocupadas sobre noches disponibles en el periodo (US14). */
public record ReporteOcupacionResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        long nochesDelPeriodo,
        double ocupacionPromedio,
        List<PorAlojamiento> porAlojamiento) {

    public record PorAlojamiento(Integer idAlojamiento, String alojamiento, long nochesOcupadas,
                                 long reservas, double ocupacion) {
    }
}
