package com.hotel.controller;

import com.hotel.dto.GeocodificacionResponse;
import com.hotel.service.GeocodificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Ubicacion del alojamiento (US04): sugiere coordenadas para una direccion. */
@RestController
@RequestMapping("/api/v1/geocodificacion")
public class GeocodificacionController {

    private final GeocodificacionService geocodificacionService;

    public GeocodificacionController(GeocodificacionService geocodificacionService) {
        this.geocodificacionService = geocodificacionService;
    }

    /** Ejemplo: /api/v1/geocodificacion?direccion=Malecon de la Reserva 610, Miraflores, Lima */
    @GetMapping
    public ResponseEntity<GeocodificacionResponse> geocodificar(@RequestParam String direccion) {
        return ResponseEntity.ok(geocodificacionService.geocodificar(direccion));
    }
}
