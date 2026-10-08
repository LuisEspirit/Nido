package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "opcion")
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idopcion")
    private Integer idopcion;

    @NotBlank(message = "El nombre de la opcion es obligatorio")
    @Column(length = 45)
    private String nombre;

    @Column(length = 45)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String ruta;

    private Short tipo;
}
