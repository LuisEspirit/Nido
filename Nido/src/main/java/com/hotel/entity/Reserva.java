package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull(message = "La fecha de entrada es obligatoria")
    private LocalDateTime entrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDateTime salida;

    @Column(length = 45)
    private String canal;

    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Double precio;

    @Column(length = 10)
    private String moneda;

    @Column(length = 45)
    private String estado;

    /** Usuario que registra el dato (auditoria de registro). Debe ser el usuario que inicio sesion. */
    @Column(name = "idUsuario", updatable = false)
    private Integer idUsuario;

    @NotNull(message = "El alojamiento es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idAlojamiento", nullable = false)
    private Alojamiento alojamiento;

    @NotNull(message = "El huesped es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idHuesped", nullable = false)
    private Huesped huesped;
}
