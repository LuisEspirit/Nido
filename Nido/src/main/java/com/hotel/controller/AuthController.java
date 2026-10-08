package com.hotel.controller;

import com.hotel.dto.LoginRequest;
import com.hotel.dto.LoginResponse;
import com.hotel.entity.Usuario;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.UsuarioRepository;
import com.hotel.security.JwtService;
import com.hotel.security.UsuarioActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Inicio de sesion (US01). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UsuarioRepository usuarioRepository, UsuarioActual usuarioActual) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserDetails userDetails;
        try {
            userDetails = (UserDetails) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.password())).getPrincipal();
        } catch (AuthenticationException e) {
            // Mensaje generico: no revela si fallo el usuario, la contrasena o si la cuenta esta desactivada.
            throw new ReglaNegocioException("Usuario o contrasena incorrectos.", HttpStatus.UNAUTHORIZED);
        }
        Usuario usuario = usuarioRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(armarRespuesta(usuario, jwtService.generarToken(userDetails)));
    }

    /** Datos del usuario autenticado y opciones de su menu. */
    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> perfil() {
        Usuario usuario = usuarioActual.usuario();
        LoginResponse r = armarRespuesta(usuario, null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idusuario", r.idusuario());
        body.put("login", r.login());
        body.put("nombres", r.nombres());
        body.put("roles", r.roles());
        body.put("opciones", r.opciones());
        return ResponseEntity.ok(body);
    }

    private LoginResponse armarRespuesta(Usuario usuario, String token) {
        List<String> roles = usuario.getRoles().stream().map(rol -> rol.getNombre().toUpperCase()).toList();
        List<LoginResponse.OpcionMenu> opciones = usuario.getRoles().stream()
                .flatMap(rol -> rol.getOpciones().stream())
                .filter(o -> o.getEstado() == null || "ACTIVO".equalsIgnoreCase(o.getEstado()))
                .map(o -> new LoginResponse.OpcionMenu(o.getNombre(), o.getRuta()))
                .distinct()
                .toList();
        return new LoginResponse(token, token == null ? null : "Bearer", jwtService.getMinutosExpiracion(),
                usuario.getIdusuario(), usuario.getLogin(),
                (usuario.getNombres() + " " + (usuario.getApellidos() == null ? "" : usuario.getApellidos())).trim(),
                roles, opciones);
    }
}
