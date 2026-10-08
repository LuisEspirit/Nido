package com.hotel.service;

import com.hotel.dto.GeocodificacionResponse;

public interface GeocodificacionService {
    GeocodificacionResponse geocodificar(String direccion);
}
