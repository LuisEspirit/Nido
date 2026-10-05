package com.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.entity.Alojamiento;

@Repository
public interface AlojamientoRepository extends JpaRepository<Alojamiento, Integer> {
	// Método para buscar por estado
    List<Alojamiento> findByEstado(String estado);
}