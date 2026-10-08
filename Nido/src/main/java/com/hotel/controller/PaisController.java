package com.hotel.controller;

import com.hotel.entity.Pais;
import com.hotel.service.PaisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/paises")
public class PaisController {

    private final PaisService paisService;

    public PaisController(PaisService paisService) {
        this.paisService = paisService;
    }

    @GetMapping
    public ResponseEntity<List<Pais>> listar() {
        return ResponseEntity.ok(paisService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pais> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(paisService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Pais> crear(@Valid @RequestBody Pais pais) {
        return new ResponseEntity<>(paisService.save(pais), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pais> actualizar(@PathVariable Integer id, @Valid @RequestBody Pais pais) {
        return ResponseEntity.ok(paisService.update(id, pais));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        paisService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
