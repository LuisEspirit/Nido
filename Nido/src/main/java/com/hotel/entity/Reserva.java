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
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReserva")
    private Integer idReserva;

    private LocalDateTime entrada;
    private LocalDateTime salida;

    @Column(length = 45)
    private String canal;

    private Double precio;

    @Column(length = 10)
    private String moneda;

    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    private Alojamiento alojamiento;

    @ManyToOne
    @JoinColumn(name = "idHuesped", nullable = false)
    private Huesped huesped;
}