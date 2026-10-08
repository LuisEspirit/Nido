package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    /** INGRESO o GASTO. */
    @Column(length = 45)
    private String tipo;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a 0")
    private Double importe;

    @Column(length = 10)
    private String moneda;

    private LocalDateTime fecha;

    /** PAGADO, PENDIENTE o ANULADO. */
    @Column(length = 45)
    private String estado;

    @NotNull(message = "La reserva es obligatoria")
    @ManyToOne
    @JoinColumn(name = "idReserva", nullable = false)
    @JsonIgnoreProperties({"huesped"})
    private Reserva reserva;
}
