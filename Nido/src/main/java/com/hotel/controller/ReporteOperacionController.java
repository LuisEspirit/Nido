package com.hotel.controller;

import com.hotel.dto.ReporteOperacionResponse;
import com.hotel.service.ReporteOperacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Reporte de operacion (HU15). Los errores de parametros los responde GlobalExceptionHandler. */
@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteOperacionController {

    private final ReporteOperacionService reporteOperacionService;

    public ReporteOperacionController(ReporteOperacionService reporteOperacionService) {
        this.reporteOperacionService = reporteOperacionService;
    }

    @GetMapping("/operacion")
    public ResponseEntity<ReporteOperacionResponse> reporteOperacion(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteOperacionService.obtenerReporte(fechaInicio, fechaFin));
    }
}
