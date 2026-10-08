package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
    @Column(length = 8)
    private String dni;

    @NotBlank(message = "El login es obligatorio")
    @Size(max = 15, message = "El login admite como maximo 15 caracteres")
    @Column(length = 15)
    private String login;

    /** Se recibe en texto plano y se guarda cifrada con BCrypt. Nunca se devuelve en las respuestas. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 200)
    private String password;

    @Email(message = "El correo no tiene un formato valido")
    @Column(length = 45)
    private String correo;

    @Column(name = "fechaRegistro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fechaNacimiento")
    private LocalDate fechaNacimiento;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    /** Especialidad del personal operativo (LIMPIEZA, MANTENIMIENTO...). */
    @Column(length = 45)
    private String especialidad;

    /** ACTIVO o INACTIVO. Un usuario INACTIVO no puede iniciar sesion. */
    @Column(length = 45)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "idubigeo")
    private Ubigeo ubigeo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_has_rol",
            joinColumns = @JoinColumn(name = "idusuario"),
            inverseJoinColumns = @JoinColumn(name = "idrol"))
    @JsonIgnoreProperties("opciones")
    private List<Rol> roles = new ArrayList<>();
}
