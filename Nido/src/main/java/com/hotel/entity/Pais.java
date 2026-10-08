package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pais")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPais")
    private Integer idPais;

    @Size(min = 2, max = 2, message = "El codigo ISO debe tener 2 letras")
    @Column(columnDefinition = "CHAR(2)")
    private String iso;

    @NotBlank(message = "El nombre del pais es obligatorio")
    @Column(length = 80)
    private String nombre;
}
