package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de los PATCH .../estado, por ejemplo {"estado": "INACTIVO"}. */
public record EstadoRequest(@NotBlank(message = "El estado es obligatorio") String estado) {
}
