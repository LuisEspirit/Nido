package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Foto que demuestra el estado o el resultado de un servicio (US12). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evidencia")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvidencia")
    private Integer idEvidencia;

    @Column(name = "nombreArchivo", length = 200)
    private String nombreArchivo;

    /** Ruta interna del archivo en el servidor; no se expone en el JSON. */
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String ruta;

    @Column(name = "tipoArchivo", length = 45)
    private String tipoArchivo;

    private Long tamanio;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "idServicio", nullable = false)
    @JsonIgnoreProperties({"alojamiento", "reserva", "usuario", "checklist"})
    private Servicio servicio;

    /** Autor de la evidencia. */
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    @JsonIgnoreProperties({"roles", "ubigeo", "direccion", "fechaNacimiento", "fechaRegistro", "dni"})
    private Usuario usuario;
}
