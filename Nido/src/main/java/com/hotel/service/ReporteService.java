package com.hotel.service;

import com.hotel.dto.CalendarioEvento;
import com.hotel.dto.ReporteIngresosResponse;
import com.hotel.dto.ReporteOcupacionResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    ReporteIngresosResponse ingresos(LocalDate fechaInicio, LocalDate fechaFin, String moneda);
    ReporteOcupacionResponse ocupacion(LocalDate fechaInicio, LocalDate fechaFin);
    List<CalendarioEvento> calendario(LocalDate fechaInicio, LocalDate fechaFin, Integer idAlojamiento);
}
