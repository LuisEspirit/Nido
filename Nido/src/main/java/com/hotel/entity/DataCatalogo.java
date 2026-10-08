package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "datacatalogo")
public class DataCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDataCatalogo")
    private Integer idDataCatalogo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 45)
    private String estado;

    @NotNull(message = "El catalogo es obligatorio")
    @ManyToOne
    @JoinColumn(name = "idCatalogo", nullable = false)
    private Catalogo catalogo;
}
