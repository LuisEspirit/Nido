package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "huesped")
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHuesped")
    private Integer idHuesped;

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(length = 100)
    private String apellidos;

    @Email(message = "El correo no tiene un formato valido")
    @Column(length = 100)
    private String correo;

    @Column(length = 45)
    private String telefono;

    /** Usuario que registra el dato (auditoria de registro). Debe ser el usuario que inicio sesion. */
    @Column(name = "idUsuario", updatable = false)
    private Integer idUsuario;

    @NotNull(message = "Debe indicar si el huesped otorgo su consentimiento")
    private Boolean consentimiento;
}
