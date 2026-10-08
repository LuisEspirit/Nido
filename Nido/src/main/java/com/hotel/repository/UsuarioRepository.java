package com.hotel.repository;

import com.hotel.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    /** Personal operativo (rol PERSONAL). Si soloActivos es true, excluye a los INACTIVOS (US09). */
    @Query("""
            select distinct u from Usuario u join u.roles r
            where upper(r.nombre) = 'PERSONAL'
              and (:soloActivos = false or upper(u.estado) = 'ACTIVO')
            order by u.nombres
            """)
    List<Usuario> buscarPersonal(@Param("soloActivos") boolean soloActivos);
}
