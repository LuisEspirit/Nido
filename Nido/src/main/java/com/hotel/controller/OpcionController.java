package com.hotel.controller;

import com.hotel.entity.Opcion;
import com.hotel.service.OpcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/opciones")
public class OpcionController {

    private final OpcionService opcionService;

    public OpcionController(OpcionService opcionService) {
        this.opcionService = opcionService;
    }

    @GetMapping
    public ResponseEntity<List<Opcion>> listar() {
        return ResponseEntity.ok(opcionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Opcion> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(opcionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Opcion> crear(@Valid @RequestBody Opcion opcion) {
        return new ResponseEntity<>(opcionService.save(opcion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Opcion> actualizar(@PathVariable Integer id, @Valid @RequestBody Opcion opcion) {
        return ResponseEntity.ok(opcionService.update(id, opcion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        opcionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
