package com.hotel.controller;

import com.hotel.dto.DisponibilidadResponse;
import com.hotel.entity.Reserva;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodas() {
        return new ResponseEntity<>(reservaService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Integer id) {
        return reservaService.findById(id)
                .map(reserva -> new ResponseEntity<>(reserva, HttpStatus.OK))
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }

    @GetMapping("/alojamiento/{idAlojamiento}")
    public ResponseEntity<List<Reserva>> listarPorAlojamiento(@PathVariable Integer idAlojamiento) {
        return ResponseEntity.ok(reservaService.listarPorAlojamiento(idAlojamiento));
    }

    /** Ejemplo: /api/v1/reservas/disponibilidad?idAlojamiento=1&entrada=2026-10-11T14:00:00&salida=2026-10-12T11:00:00 */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> disponibilidad(
            @RequestParam Integer idAlojamiento,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime entrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime salida) {
        return ResponseEntity.ok(reservaService.consultarDisponibilidad(idAlojamiento, entrada, salida));
    }

    @PostMapping
    public ResponseEntity<Reserva> crear(@Valid @RequestBody Reserva reserva) {
        return new ResponseEntity<>(reservaService.save(reserva), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizar(@PathVariable Integer id, @Valid @RequestBody Reserva reserva) {
        return new ResponseEntity<>(reservaService.update(id, reserva), HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        reservaService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
