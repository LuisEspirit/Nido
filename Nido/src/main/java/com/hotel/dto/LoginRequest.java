package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El login es obligatorio") String login,
        @NotBlank(message = "La contrasena es obligatoria") String password) {
}
