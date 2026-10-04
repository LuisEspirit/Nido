package com.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ubigeo")
public class Ubigeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idubigeo")
    private Integer idubigeo;

    @Column(length = 45)
    private String departamento;

    @Column(length = 45)
    private String provincia;

    @Column(length = 45)
    private String distrito;
}