package com.hotel.controller;

import com.hotel.dto.CalendarioEvento;
import com.hotel.dto.ReporteIngresosResponse;
import com.hotel.dto.ReporteOcupacionResponse;
import com.hotel.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Reportes de ingresos y ocupacion (US14) y calendario (US08). Fechas en formato yyyy-MM-dd. */
@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ingresos")
    public ResponseEntity<ReporteIngresosResponse> ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "PEN") String moneda) {
        return ResponseEntity.ok(reporteService.ingresos(fechaInicio, fechaFin, moneda));
    }

    @GetMapping("/ocupacion")
    public ResponseEntity<ReporteOcupacionResponse> ocupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.ocupacion(fechaInicio, fechaFin));
    }

    @GetMapping("/calendario")
    public ResponseEntity<List<CalendarioEvento>> calendario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer idAlojamiento) {
        return ResponseEntity.ok(reporteService.calendario(fechaInicio, fechaFin, idAlojamiento));
    }
}
