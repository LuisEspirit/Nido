package com.hotel.controller;

import com.hotel.dto.ChecklistItem;
import com.hotel.dto.EstadoRequest;
import com.hotel.entity.Servicio;
import com.hotel.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Servicios de limpieza, mantenimiento e inspeccion (US10, US11). */
@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<List<Servicio>> listar() {
        return ResponseEntity.ok(servicioService.findAll());
    }

    /** Servicios asignados al usuario que inicio sesion (vista "Mis tareas" del personal). */
    @GetMapping("/mis-servicios")
    public ResponseEntity<List<Servicio>> misServicios() {
        return ResponseEntity.ok(servicioService.misServicios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(servicioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Servicio> crear(@Valid @RequestBody Servicio servicio) {
        return new ResponseEntity<>(servicioService.save(servicio), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> actualizar(@PathVariable Integer id, @Valid @RequestBody Servicio servicio) {
        return ResponseEntity.ok(servicioService.update(id, servicio));
    }

    /** Ejemplo: {"estado": "ACEPTADO"}. COMPLETADO exige checklist obligatorio hecho y al menos una foto. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Servicio> cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(servicioService.cambiarEstado(id, request.estado()));
    }

    /** Ejemplo: [{"item": "Cambiar sabanas y toallas", "hecho": true}] */
    @PatchMapping("/{id}/checklist")
    public ResponseEntity<Servicio> actualizarChecklist(@PathVariable Integer id,
                                                        @RequestBody List<ChecklistItem> items) {
        return ResponseEntity.ok(servicioService.actualizarChecklist(id, items));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        servicioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
