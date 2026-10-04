package com.hotel.controller;

import com.hotel.entity.Huesped;
import com.hotel.service.HuespedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/huespedes")
@CrossOrigin(origins = "*")
public class HuespedController {

    private final HuespedService huespedService;

    public HuespedController(HuespedService huespedService) {
        this.huespedService = huespedService;
    }

    @GetMapping
    public ResponseEntity<List<Huesped>> listarTodos() {
        return new ResponseEntity<>(huespedService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Huesped> obtenerPorId(@PathVariable Integer id) {
        return huespedService.findById(id)
                .map(huesped -> new ResponseEntity<>(huesped, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Huesped> crear(@RequestBody Huesped huesped) {
        return new ResponseEntity<>(huespedService.save(huesped), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Huesped> actualizar(@PathVariable Integer id, @RequestBody Huesped huesped) {
        try {
            return new ResponseEntity<>(huespedService.update(id, huesped), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        huespedService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}