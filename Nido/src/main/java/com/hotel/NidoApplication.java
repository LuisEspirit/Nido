package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NidoApplication {

	public static void main(String[] args) {
		// Las fechas de reservas y servicios son hora de Peru. Sin esto, en un servidor con hora UTC (como AWS)
		// MySQL devolveria las horas desplazadas 5 horas (serverTimezone=America/Lima en la URL de conexion).
		TimeZone.setDefault(TimeZone.getTimeZone(System.getenv().getOrDefault("NIDO_ZONA_HORARIA", "America/Lima")));
		SpringApplication.run(NidoApplication.class, args);
	}

}
