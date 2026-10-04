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
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private Integer idPago;

    @Column(length = 45)
    private String tipo;

    private Double importe;

    @Column(length = 10)
    private String moneda;

    private LocalDateTime fecha;

    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;
}