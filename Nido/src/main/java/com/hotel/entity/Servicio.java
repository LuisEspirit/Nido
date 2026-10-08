package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idServicio")
    private Integer idServicio;

    /** LIMPIEZA, MANTENIMIENTO o INSPECCION. */
    @NotBlank(message = "El tipo de servicio es obligatorio")
    @Column(length = 45)
    private String tipo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime inicio;

    private LocalDateTime fin;

    /** PENDIENTE, ASIGNADO, ACEPTADO, RECHAZADO, EN_PROCESO, COMPLETADO o CANCELADO. */
    @Column(length = 45)
    private String estado;

    /** Lista JSON de items: [{"item":"...","obligatorio":true,"hecho":false}] (US11). */
    @Column(columnDefinition = "TEXT")
    private String checklist;

    @NotNull(message = "El alojamiento es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    @JsonIgnoreProperties({"propietario"})
    private Alojamiento alojamiento;

    @ManyToOne
    @JoinColumn(name = "idReserva")
    @JsonIgnoreProperties({"alojamiento", "huesped"})
    private Reserva reserva;

    /** Personal operativo asignado. */
    @NotNull(message = "El personal asignado es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    @JsonIgnoreProperties({"roles", "dni", "direccion", "fechaNacimiento", "fechaRegistro", "ubigeo"})
    private Usuario usuario;
}
