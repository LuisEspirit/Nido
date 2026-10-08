package com.hotel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Coordenadas confirmadas por el propietario (US04). */
public record UbicacionRequest(
        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        Double longitud) {
}
