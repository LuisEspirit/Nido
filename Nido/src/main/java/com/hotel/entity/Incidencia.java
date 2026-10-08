package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "incidencia")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIncidencia")
    private Integer idIncidencia;

    @NotBlank(message = "La categoria es obligatoria")
    @Column(length = 45)
    private String categoria;

    /** BAJA, MEDIA, ALTA o CRITICA. */
    @NotBlank(message = "La prioridad es obligatoria")
    @Column(length = 45)
    private String prioridad;

    @NotBlank(message = "La descripcion es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** ABIERTA, EN_PROCESO, RESUELTA o CERRADA. Al registrarse queda ABIERTA. */
    @Column(length = 45)
    private String estado;

    /** Usuario que registra el dato (auditoria de registro). Debe ser el usuario que inicio sesion. */
    @Column(name = "idUsuario", updatable = false)
    private Integer idUsuario;

    @NotNull(message = "El alojamiento es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    @JsonIgnoreProperties({"propietario"})
    private Alojamiento alojamiento;

    @ManyToOne
    @JoinColumn(name = "idReserva")
    @JsonIgnoreProperties({"alojamiento", "huesped"})
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "idServicio")
    @JsonIgnoreProperties({"alojamiento", "reserva", "personal", "checklist"})
    private Servicio servicio;
}
