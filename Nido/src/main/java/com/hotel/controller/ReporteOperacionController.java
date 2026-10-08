package com.hotel.controller;

import com.hotel.dto.ReporteOperacionResponse;
import com.hotel.service.ReporteOperacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.Map;

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> parametroFaltante(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Falta el parámetro obligatorio '" + e.getParameterName()
                        + "' (formato yyyy-MM-dd)."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> formatoInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "El parámetro '" + e.getName()
                        + "' tiene un formato inválido. Use yyyy-MM-dd, por ejemplo 2026-10-01."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}