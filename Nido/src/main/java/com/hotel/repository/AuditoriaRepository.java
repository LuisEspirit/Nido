package com.hotel.repository;

import com.hotel.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    @Query("""
            select a from Auditoria a
            where (:entidad is null or a.entidad = :entidad)
              and (:usuario is null or a.usuario = :usuario)
            order by a.fecha desc
            """)
    List<Auditoria> buscar(@Param("entidad") String entidad, @Param("usuario") String usuario);
}
