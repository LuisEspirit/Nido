package com.hotel.repository;

import com.hotel.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Integer> {

    /** Huespedes con el mismo correo (sin distinguir mayusculas), distintos del indicado. */
    List<Huesped> findByCorreoIgnoreCaseAndIdHuespedNot(String correo, Integer idHuesped);

    /** Huespedes con los mismos nombres, apellidos y telefono, distintos del indicado. */
    List<Huesped> findByNombresIgnoreCaseAndApellidosIgnoreCaseAndTelefonoAndIdHuespedNot(
            String nombres, String apellidos, String telefono, Integer idHuesped);
}
