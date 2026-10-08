package com.hotel.service;

import com.hotel.dto.ReporteOperacionResponse;

import java.time.LocalDate;

public interface ReporteOperacionService {
    ReporteOperacionResponse obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin);
}