package com.hotel.service.impl;

import com.hotel.entity.Rol;
import com.hotel.entity.Usuario;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.RolRepository;
import com.hotel.repository.UsuarioRepository;
import com.hotel.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<String> ESTADOS = Set.of("ACTIVO", "INACTIVO");
    private static final int MIN_PASSWORD = 8;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuarioRepository.existsByLoginIgnoreCase(usuario.getLogin())) {
            throw new ReglaNegocioException("Ya existe un usuario con el login '" + usuario.getLogin() + "'.");
        }
        validarPassword(usuario.getPassword());
        usuario.setIdusuario(null);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado(usuario.getEstado() == null ? "ACTIVO" : normalizarEstado(usuario.getEstado()));
        usuario.setRoles(cargarRoles(usuario.getRoles()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario update(Integer id, Usuario usuario) {
        Usuario existing = findById(id);
        if (!existing.getLogin().equalsIgnoreCase(usuario.getLogin())
                && usuarioRepository.existsByLoginIgnoreCase(usuario.getLogin())) {
            throw new ReglaNegocioException("Ya existe un usuario con el login '" + usuario.getLogin() + "'.");
        }
        existing.setNombres(usuario.getNombres());
        existing.setApellidos(usuario.getApellidos());
        existing.setDni(usuario.getDni());
        existing.setLogin(usuario.getLogin());
        existing.setCorreo(usuario.getCorreo());
        existing.setFechaNacimiento(usuario.getFechaNacimiento());
        existing.setDireccion(usuario.getDireccion());
        existing.setEspecialidad(usuario.getEspecialidad());
        existing.setUbigeo(usuario.getUbigeo());
        if (usuario.getEstado() != null) {
            existing.setEstado(normalizarEstado(usuario.getEstado()));
        }
        // La contrasena solo cambia si se envia una nueva
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            validarPassword(usuario.getPassword());
            existing.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            existing.setRoles(cargarRoles(usuario.getRoles()));
        }
        return usuarioRepository.save(existing);
    }

    @Override
    @Transactional
    public Usuario cambiarEstado(Integer id, String estado) {
        Usuario usuario = findById(id);
        usuario.setEstado(normalizarEstado(estado));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        usuarioRepository.delete(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPersonal(boolean soloActivos) {
        return usuarioRepository.buscarPersonal(soloActivos);
    }

    @Override
    @Transactional
    public Usuario registrarPersonal(Usuario usuario) {
        // El personal operativo siempre se registra con el rol PERSONAL, sin importar lo que se envie
        Rol personal = new Rol();
        personal.setNombre("PERSONAL");
        usuario.setRoles(new ArrayList<>(List.of(personal)));
        return save(usuario);
    }

    @Override
    @Transactional
    public Usuario cambiarEstadoPersonal(Integer id, String estado) {
        Usuario usuario = findById(id);
        boolean esPersonal = usuario.getRoles().stream().anyMatch(r -> "PERSONAL".equalsIgnoreCase(r.getNombre()));
        if (!esPersonal) {
            throw new ReglaNegocioException("El usuario " + id + " no pertenece al personal operativo.",
                    HttpStatus.BAD_REQUEST);
        }
        usuario.setEstado(normalizarEstado(estado));
        return usuarioRepository.save(usuario);
    }

    /** Busca cada rol por id o por nombre; si no se envia ninguno, el usuario queda sin rol. */
    private List<Rol> cargarRoles(List<Rol> roles) {
        List<Rol> resultado = new ArrayList<>();
        if (roles == null) {
            return resultado;
        }
        for (Rol rol : roles) {
            Rol encontrado;
            if (rol.getIdrol() != null) {
                encontrado = rolRepository.findById(rol.getIdrol())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol", rol.getIdrol()));
            } else if (rol.getNombre() != null) {
                encontrado = rolRepository.findByNombreIgnoreCase(rol.getNombre())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol", rol.getNombre()));
            } else {
                throw new ReglaNegocioException("Cada rol debe indicar idrol o nombre.", HttpStatus.BAD_REQUEST);
            }
            resultado.add(encontrado);
        }
        return resultado;
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ReglaNegocioException("La contrasena debe tener al menos " + MIN_PASSWORD + " caracteres.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizarEstado(String estado) {
        String valor = estado.trim().toUpperCase();
        if (!ESTADOS.contains(valor)) {
            throw new ReglaNegocioException("Estado invalido: " + estado + ". Use ACTIVO o INACTIVO.",
                    HttpStatus.BAD_REQUEST);
        }
        return valor;
    }
}
