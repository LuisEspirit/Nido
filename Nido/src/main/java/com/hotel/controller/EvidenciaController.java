package com.hotel.controller;

import com.hotel.entity.Evidencia;
import com.hotel.service.EvidenciaService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Evidencias fotograficas de los servicios (US12). */
@RestController
@RequestMapping("/api/v1")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping("/servicios/{idServicio}/evidencias")
    public ResponseEntity<List<Evidencia>> listar(@PathVariable Integer idServicio) {
        return ResponseEntity.ok(evidenciaService.listarPorServicio(idServicio));
    }

    /**
     * Subir una foto: multipart/form-data con los campos "archivo" (JPG, PNG o WEBP, maximo 5 MB)
     * e "idUsuario" (quien la registra).
     */
    @PostMapping(value = "/servicios/{idServicio}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Evidencia> subir(@PathVariable Integer idServicio,
                                           @RequestParam("idUsuario") Integer idUsuario,
                                           @RequestParam("archivo") MultipartFile archivo) {
        return new ResponseEntity<>(evidenciaService.subir(idServicio, idUsuario, archivo), HttpStatus.CREATED);
    }

    @GetMapping("/evidencias/{id}")
    public ResponseEntity<Evidencia> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(evidenciaService.findById(id));
    }

    /** Descarga la foto para mostrarla en el navegador. */
    @GetMapping("/evidencias/{id}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable Integer id) {
        Evidencia evidencia = evidenciaService.findById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(evidencia.getTipoArchivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + evidencia.getNombreArchivo() + "\"")
                .body(evidenciaService.archivo(evidencia));
    }

    @DeleteMapping("/evidencias/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        evidenciaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
