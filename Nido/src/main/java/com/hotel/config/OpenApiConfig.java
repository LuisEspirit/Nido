package com.hotel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacion de la API en /swagger-ui.html con boton "Authorize" para el token JWT. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nidoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nido API")
                        .version("v1")
                        .description("API REST de Nido - gestion de alojamientos temporales. "
                                + "Primero ejecute POST /api/v1/auth/login, copie el token y pulse Authorize."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
