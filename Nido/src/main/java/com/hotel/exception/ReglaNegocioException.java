package com.hotel.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operacion viola una regla del negocio
 * (por ejemplo, una reserva solapada). Por defecto responde 409 Conflict.
 */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus status;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, HttpStatus.CONFLICT);
    }

    public ReglaNegocioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
