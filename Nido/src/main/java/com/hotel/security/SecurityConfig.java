package com.hotel.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Seguridad por roles (US01, US02).
 * Roles: ADMIN, PROPIETARIO y PERSONAL (tabla rol).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String PROPIETARIO = "PROPIETARIO";
    private static final String PERSONAL = "PERSONAL";

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Publico: inicio de sesion y documentacion de la API
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                // Frontend estatico (las paginas piden el token al llamar a la API)
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/login.html", "/favicon.svg", "/app/**",
                        "/css/**", "/js/**", "/img/**", "/fonts/**", "/vendor/**").permitAll()

                // Personal operativo: sus servicios, checklist, evidencias e incidencias
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/mis-servicios").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/*").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/servicios/*/estado", "/api/v1/servicios/*/checklist").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers("/api/v1/servicios/*/evidencias", "/api/v1/evidencias/**").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers(HttpMethod.GET, "/api/v1/incidencias", "/api/v1/incidencias/*").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers(HttpMethod.POST, "/api/v1/incidencias").hasAnyRole(ADMIN, PROPIETARIO, PERSONAL)
                .requestMatchers("/api/v1/auth/**").authenticated()

                // Catalogos: cualquier usuario autenticado consulta; solo ADMIN modifica
                .requestMatchers(HttpMethod.GET, "/api/v1/ubigeos/**", "/api/v1/paises/**",
                        "/api/v1/catalogos/**", "/api/v1/datacatalogos/**").authenticated()
                .requestMatchers("/api/v1/ubigeos/**", "/api/v1/paises/**",
                        "/api/v1/catalogos/**", "/api/v1/datacatalogos/**").hasRole(ADMIN)

                // Gestion de personal (US09): ADMIN y PROPIETARIO
                .requestMatchers("/api/v1/usuarios/personal", "/api/v1/usuarios/personal/**").hasAnyRole(ADMIN, PROPIETARIO)

                // Administracion: usuarios, roles, opciones y auditoria (US02, US22)
                .requestMatchers("/api/v1/usuarios/**", "/api/v1/roles/**", "/api/v1/opciones/**",
                        "/api/v1/auditorias/**").hasRole(ADMIN)

                // Resto de la API: propietario y administrador
                .requestMatchers("/api/v1/**").hasAnyRole(ADMIN, PROPIETARIO)
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escribirError(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                                "Debe iniciar sesion y enviar un token valido en el header Authorization.", req.getRequestURI()))
                .accessDeniedHandler((req, res, ex) ->
                        escribirError(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                "No tiene permisos para realizar esta operacion.", req.getRequestURI())))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private static void escribirError(HttpServletResponse res, int status, String error, String mensaje, String ruta)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"mensaje\":\""
                + mensaje + "\",\"ruta\":\"" + ruta + "\"}");
    }
}
