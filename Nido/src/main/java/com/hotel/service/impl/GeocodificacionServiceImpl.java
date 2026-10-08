package com.hotel.service.impl;

import com.hotel.dto.GeocodificacionResponse;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.service.GeocodificacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Geocodificacion de direcciones con la API publica de OpenStreetMap (Nominatim), sin clave de acceso (US04).
 * Politica de uso: maximo 1 consulta por segundo y un User-Agent que identifique a la aplicacion.
 */
@Service
public class GeocodificacionServiceImpl implements GeocodificacionService {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final String urlBase;

    public GeocodificacionServiceImpl(ObjectMapper objectMapper,
                                      @Value("${nido.geocodificacion.url:https://nominatim.openstreetmap.org/search}") String urlBase) {
        this.objectMapper = objectMapper;
        this.urlBase = urlBase;
    }

    @Override
    public GeocodificacionResponse geocodificar(String direccion) {
        if (direccion == null || direccion.trim().length() < 5) {
            throw new ReglaNegocioException("Ingrese una direccion de al menos 5 caracteres.", HttpStatus.BAD_REQUEST);
        }
        String url = urlBase + "?format=jsonv2&limit=1&accept-language=es&q="
                + URLEncoder.encode(direccion.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Nido/1.0 (proyecto academico UPC)")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw servicioNoDisponible();
            }
            JsonNode resultados = objectMapper.readTree(response.body());
            if (!resultados.isArray() || resultados.isEmpty()) {
                throw new ReglaNegocioException("No encontramos esa direccion. Revisala o ingresa las coordenadas manualmente.",
                        HttpStatus.NOT_FOUND);
            }
            JsonNode lugar = resultados.get(0);
            return new GeocodificacionResponse(direccion.trim(), lugar.path("display_name").asText(),
                    lugar.path("lat").asDouble(), lugar.path("lon").asDouble(), "OpenStreetMap Nominatim");
        } catch (IOException e) {
            throw servicioNoDisponible();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw servicioNoDisponible();
        }
    }

    private ReglaNegocioException servicioNoDisponible() {
        return new ReglaNegocioException("El servicio de mapas no esta disponible. Ingresa las coordenadas manualmente.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
