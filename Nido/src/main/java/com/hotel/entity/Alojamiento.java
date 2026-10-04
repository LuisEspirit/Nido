package com.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alojamiento")
public class Alojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlojamiento")
    private Integer idAlojamiento;

    @Column(length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private Double latitud;
    private Double longitud;
    private Integer capacidad;

    @Column(name = "precioBase")
    private Double precioBase;

    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idPropietario", nullable = false)
    private Usuario propietario;

    @ManyToOne
    @JoinColumn(name = "idubigeo")
    private Ubigeo ubigeo;
}