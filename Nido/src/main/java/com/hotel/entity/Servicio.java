package com.hotel.entity;

import jakarta.persistence.*;
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

    @Column(length = 45)
    private String tipo;

    private LocalDateTime inicio;
    private LocalDateTime fin;

    @Column(length = 45)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String checklist;

    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    private Alojamiento alojamiento;

    @ManyToOne
    @JoinColumn(name = "idReserva")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;
}