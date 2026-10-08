package com.hotel.controller;

import com.hotel.dto.EstadoRequest;
import com.hotel.entity.Usuario;
import com.hotel.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administracion de usuarios y sus roles (US02): solo ADMIN.
 * Gestion del personal operativo (US09): ADMIN y PROPIETARIO, en /personal.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario usuario) {
        return new ResponseEntity<>(usuarioService.save(usuario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Integer id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.update(id, usuario));
    }

    /** Activa o desactiva la cuenta. Un usuario INACTIVO no puede iniciar sesion. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Personal operativo (US09)
    // ------------------------------------------------------------------

    @GetMapping("/personal")
    public ResponseEntity<List<Usuario>> listarPersonal(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(usuarioService.listarPersonal(soloActivos));
    }

    @PostMapping("/personal")
    public ResponseEntity<Usuario> registrarPersonal(@Valid @RequestBody Usuario usuario) {
        return new ResponseEntity<>(usuarioService.registrarPersonal(usuario), HttpStatus.CREATED);
    }

    @PatchMapping("/personal/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstadoPersonal(@PathVariable Integer id,
                                                         @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarEstadoPersonal(id, request.estado()));
    }
}
