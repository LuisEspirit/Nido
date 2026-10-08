package com.hotel.controller;

import com.hotel.entity.DataCatalogo;
import com.hotel.service.DataCatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datacatalogos")
public class DataCatalogoController {

    private final DataCatalogoService dataCatalogoService;

    public DataCatalogoController(DataCatalogoService dataCatalogoService) {
        this.dataCatalogoService = dataCatalogoService;
    }

    @GetMapping
    public ResponseEntity<List<DataCatalogo>> listar() {
        return ResponseEntity.ok(dataCatalogoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataCatalogo> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(dataCatalogoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DataCatalogo> crear(@Valid @RequestBody DataCatalogo dataCatalogo) {
        return new ResponseEntity<>(dataCatalogoService.save(dataCatalogo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataCatalogo> actualizar(@PathVariable Integer id, @Valid @RequestBody DataCatalogo dataCatalogo) {
        return ResponseEntity.ok(dataCatalogoService.update(id, dataCatalogo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        dataCatalogoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
