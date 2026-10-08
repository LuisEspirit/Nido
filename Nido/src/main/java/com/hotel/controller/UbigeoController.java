package com.hotel.controller;

import com.hotel.entity.Ubigeo;
import com.hotel.service.UbigeoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubigeos")
public class UbigeoController {

    private final UbigeoService ubigeoService;

    public UbigeoController(UbigeoService ubigeoService) {
        this.ubigeoService = ubigeoService;
    }

    @GetMapping
    public ResponseEntity<List<Ubigeo>> listar() {
        return ResponseEntity.ok(ubigeoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ubigeo> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ubigeoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Ubigeo> crear(@Valid @RequestBody Ubigeo ubigeo) {
        return new ResponseEntity<>(ubigeoService.save(ubigeo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ubigeo> actualizar(@PathVariable Integer id, @Valid @RequestBody Ubigeo ubigeo) {
        return ResponseEntity.ok(ubigeoService.update(id, ubigeo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ubigeoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
