package com.hotel.security;

import com.hotel.entity.Usuario;
import com.hotel.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carga el usuario desde la tabla usuario y sus roles desde usuario_has_rol. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(usuario.getLogin())
                .password(usuario.getPassword())
                .disabled(!"ACTIVO".equalsIgnoreCase(usuario.getEstado()))
                .authorities(usuario.getRoles().stream()
                        .filter(r -> r.getEstado() == null || "ACTIVO".equalsIgnoreCase(r.getEstado()))
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre().toUpperCase()))
                        .toList())
                .build();
    }
}
