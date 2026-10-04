package com.hotel.entity;

import jakarta.persistence.*;
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

    @Column(length = 45)
    private String categoria;

    @Column(length = 45)
    private String prioridad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    private Alojamiento alojamiento;

    @ManyToOne
    @JoinColumn(name = "idReserva")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "idServicio")
    private Servicio servicio;
}