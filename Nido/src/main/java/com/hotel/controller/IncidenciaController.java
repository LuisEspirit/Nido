package com.hotel.controller;

import com.hotel.dto.EstadoRequest;
import com.hotel.entity.Incidencia;
import com.hotel.service.IncidenciaService;
import com.hotel.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidencias")
@CrossOrigin(origins = "*")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    /** Filtros opcionales: estado, prioridad e idAlojamiento. */
    @GetMapping
    public ResponseEntity<List<Incidencia>> listarTodas(@RequestParam(required = false) String estado,
                                                        @RequestParam(required = false) String prioridad,
                                                        @RequestParam(required = false) Integer idAlojamiento) {
        return new ResponseEntity<>(incidenciaService.buscar(estado, prioridad, idAlojamiento), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incidencia> obtenerPorId(@PathVariable Integer id) {
        return incidenciaService.findById(id)
                .map(incidencia -> new ResponseEntity<>(incidencia, HttpStatus.OK))
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia", id));
    }

    @PostMapping
    public ResponseEntity<Incidencia> crear(@Valid @RequestBody Incidencia incidencia) {
        return new ResponseEntity<>(incidenciaService.save(incidencia), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incidencia> actualizar(@PathVariable Integer id, @Valid @RequestBody Incidencia incidencia) {
        return new ResponseEntity<>(incidenciaService.update(id, incidencia), HttpStatus.OK);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Incidencia> cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(incidenciaService.cambiarEstado(id, request.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        incidenciaService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}