package com.hotel.config;

import com.hotel.entity.Auditoria;
import com.hotel.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Registra en la tabla auditoria cada accion critica completada con exito (US22):
 * crear (POST), actualizar (PUT/PATCH) y eliminar (DELETE).
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final String PREFIJO = "/api/v1/";

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaInterceptor(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String operacion = switch (request.getMethod()) {
            case "POST" -> "CREAR";
            case "PUT", "PATCH" -> "ACTUALIZAR";
            case "DELETE" -> "ELIMINAR";
            default -> null;
        };
        String uri = request.getRequestURI();
        if (operacion == null || ex != null || response.getStatus() >= 300
                || !uri.startsWith(PREFIJO) || uri.startsWith(PREFIJO + "auth")) {
            return;
        }
        String[] partes = uri.substring(PREFIJO.length()).split("/");
        String idRegistro = partes.length > 1 && partes[1].matches("\\d+") ? partes[1] : null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(auth == null ? "anonimo" : auth.getName());
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setEntidad(partes[0]);
        auditoria.setOperacion(operacion);
        auditoria.setIdRegistro(idRegistro);
        auditoria.setDetalle(request.getMethod() + " " + uri);
        auditoriaRepository.save(auditoria);
    }
}
