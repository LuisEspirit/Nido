package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrol")
    private Integer idrol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Column(length = 45)
    private String nombre;

    @Column(length = 45)
    private String estado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_has_opcion",
            joinColumns = @JoinColumn(name = "idrol"),
            inverseJoinColumns = @JoinColumn(name = "idopcion"))
    private List<Opcion> opciones = new ArrayList<>();
}
