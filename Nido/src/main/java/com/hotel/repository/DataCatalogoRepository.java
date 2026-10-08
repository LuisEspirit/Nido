package com.hotel.repository;

import com.hotel.entity.DataCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataCatalogoRepository extends JpaRepository<DataCatalogo, Integer> {

    List<DataCatalogo> findByCatalogo_IdCatalogo(Integer idCatalogo);
}
