package com.hotel.repository;

import com.hotel.entity.Alojamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlojamientoRepository extends JpaRepository<Alojamiento, Integer> {

    // Metodo para buscar por estado (HU-01)
    List<Alojamiento> findByEstado(String estado);

    List<Alojamiento> findByEstadoAndPropietario_Idusuario(String estado, Integer idPropietario);

    List<Alojamiento> findByPropietario_Idusuario(Integer idPropietario);

    /** Alojamientos con la misma direccion (sin distinguir mayusculas ni espacios), distintos del indicado. */
    @Query("select a from Alojamiento a where lower(trim(a.direccion)) = lower(trim(:direccion)) "
            + "and a.idAlojamiento <> :idActual")
    List<Alojamiento> buscarPorDireccion(@Param("direccion") String direccion, @Param("idActual") Integer idActual);

    /** Alojamientos del mismo propietario con el mismo nombre, distintos del indicado. */
    @Query("select a from Alojamiento a where lower(trim(a.nombre)) = lower(trim(:nombre)) "
            + "and a.propietario.idusuario = :idPropietario and a.idAlojamiento <> :idActual")
    List<Alojamiento> buscarPorNombreDelPropietario(@Param("nombre") String nombre,
                                                    @Param("idPropietario") Integer idPropietario,
                                                    @Param("idActual") Integer idActual);
}
