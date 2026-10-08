package com.hotel.controller;

import com.hotel.entity.Auditoria;
import com.hotel.repository.AuditoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consulta de la bitacora de acciones criticas (US22). Solo ADMIN. */
@RestController
@RequestMapping("/api/v1/auditorias")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /** Filtros opcionales: entidad (ej. reservas) y usuario (login). */
    @GetMapping
    public ResponseEntity<List<Auditoria>> listar(@RequestParam(required = false) String entidad,
                                                  @RequestParam(required = false) String usuario) {
        return ResponseEntity.ok(auditoriaRepository.buscar(entidad, usuario));
    }
}
