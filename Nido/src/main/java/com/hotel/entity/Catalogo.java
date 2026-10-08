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
@Table(name = "catalogo")
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCatalogo")
    private Integer idCatalogo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 45)
    private String estado;
}
