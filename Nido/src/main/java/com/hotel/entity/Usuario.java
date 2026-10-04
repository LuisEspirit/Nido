package com.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer idusuario;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 8)
    private String dni;

    @Column(length = 15)
    private String login;

    @Column(length = 200)
    private String password;

    @Column(length = 45)
    private String correo;

    @Column(name = "fechaRegistro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fechaNacimiento")
    private LocalDate fechaNacimiento;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "idubigeo")
    private Ubigeo ubigeo;
}