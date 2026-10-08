package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotBlank(message = "El nombre es obligatorio")
    @Column(length = 100)
    private String nombre;

    @NotBlank(message = "La direccion es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String direccion;

    private Double latitud;
    private Double longitud;

    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    @PositiveOrZero(message = "El precio base no puede ser negativo")
    @Column(name = "precioBase")
    private Double precioBase;

    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idPropietario", nullable = false)
    @JsonIgnoreProperties({"roles", "dni", "direccion", "fechaNacimiento", "fechaRegistro", "ubigeo", "especialidad"})
    private Usuario propietario;

    @ManyToOne
    @JoinColumn(name = "idubigeo")
    private Ubigeo ubigeo;
}
