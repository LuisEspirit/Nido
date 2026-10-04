package com.hotel.controller;

import com.hotel.entity.Alojamiento;
import com.hotel.service.AlojamientoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alojamientos")
@CrossOrigin(origins = "*")
public class AlojamientoController {

    private final AlojamientoService alojamientoService;

    public AlojamientoController(AlojamientoService alojamientoService) {
        this.alojamientoService = alojamientoService;
    }

    @GetMapping
    public ResponseEntity<List<Alojamiento>> listarTodos() {
        return new ResponseEntity<>(alojamientoService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alojamiento> obtenerPorId(@PathVariable Integer id) {
        return alojamientoService.findById(id)
                .map(alojamiento -> new ResponseEntity<>(alojamiento, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Alojamiento> crear(@RequestBody Alojamiento alojamiento) {
        return new ResponseEntity<>(alojamientoService.save(alojamiento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alojamiento> actualizar(@PathVariable Integer id, @RequestBody Alojamiento alojamiento) {
        try {
            return new ResponseEntity<>(alojamientoService.update(id, alojamiento), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        alojamientoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}