package com.hotel.controller;

import com.hotel.entity.Catalogo;
import com.hotel.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public ResponseEntity<List<Catalogo>> listar() {
        return ResponseEntity.ok(catalogoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Catalogo> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Catalogo> crear(@Valid @RequestBody Catalogo catalogo) {
        return new ResponseEntity<>(catalogoService.save(catalogo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Catalogo> actualizar(@PathVariable Integer id, @Valid @RequestBody Catalogo catalogo) {
        return ResponseEntity.ok(catalogoService.update(id, catalogo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        catalogoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
