package com.hotel.repository;

import com.hotel.entity.Alojamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlojamientoRepository extends JpaRepository<Alojamiento, Integer> {

    // Metodo para buscar por estado (HU-01)
    List<Alojamiento> findByEstado(String estado);

    List<Alojamiento> findByEstadoAndPropietario_Idusuario(String estado, Integer idPropietario);

    List<Alojamiento> findByPropietario_Idusuario(Integer idPropietario);
}
