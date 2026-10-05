package com.hotel.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteOperacionResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Map<String, Long> serviciosPorEstado,
        Evidencia evidencia,
        Atrasos atrasos,
        Map<String, Long> incidenciasPorPrioridad) {

    public record Evidencia(long completa, long incompleta) {
    }

    public record Atrasos(long cantidad, List<ServicioAtrasado> servicios) {
    }

    public record ServicioAtrasado(
            Integer idServicio,
            String tipo,
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin) {
    }
}