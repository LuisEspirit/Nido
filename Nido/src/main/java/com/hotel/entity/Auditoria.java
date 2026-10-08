package com.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Registro de una accion critica: quien, cuando, sobre que entidad y que operacion (US22). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAuditoria")
    private Integer idAuditoria;

    @Column(length = 45)
    private String usuario;

    private LocalDateTime fecha;

    @Column(length = 45)
    private String entidad;

    @Column(length = 45)
    private String operacion;

    @Column(name = "idRegistro", length = 45)
    private String idRegistro;

    @Column(columnDefinition = "TEXT")
    private String detalle;
}
