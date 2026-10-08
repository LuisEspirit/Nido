package com.hotel.exception;

/** Se lanza cuando un recurso no existe. Se responde con 404 Not Found. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado con ID: " + id);
    }
}
