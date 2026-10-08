package com.hotel.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnMinutos,
        Integer idusuario,
        String login,
        String nombres,
        List<String> roles,
        List<OpcionMenu> opciones) {

    /** Opcion de menu que el frontend muestra segun el rol (tabla opcion / rol_has_opcion). */
    public record OpcionMenu(String nombre, String ruta) {
    }
}
