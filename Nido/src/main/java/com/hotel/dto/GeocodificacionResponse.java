package com.hotel.dto;

/** Coordenadas sugeridas para una direccion (US04). El propietario debe confirmarlas antes de guardar. */
public record GeocodificacionResponse(
        String direccionConsultada,
        String direccionEncontrada,
        Double latitud,
        Double longitud,
        String fuente) {
}
