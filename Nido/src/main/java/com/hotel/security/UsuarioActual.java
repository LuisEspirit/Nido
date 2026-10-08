package com.hotel.security;

import com.hotel.entity.Usuario;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Datos del usuario que hizo la peticion (segun su token JWT). */
@Component
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    public boolean esAdmin() {
        return tieneRol("ADMIN");
    }

    /** Propietario que no es administrador: solo ve sus propios alojamientos (US03). */
    public boolean esSoloPropietario() {
        return tieneRol("PROPIETARIO") && !esAdmin();
    }

    /** Personal operativo: solo ve los servicios que tiene asignados. */
    public boolean esSoloPersonal() {
        return tieneRol("PERSONAL") && !tieneRol("PROPIETARIO") && !esAdmin();
    }

    public Usuario usuario() {
        return usuarioRepository.findByLogin(login())
                .orElseThrow(() -> new ReglaNegocioException("Sesion invalida", HttpStatus.UNAUTHORIZED));
    }
}
