package com.hotel.service.impl;

import com.hotel.dto.ReporteOperacionResponse;
import com.hotel.entity.Servicio;
import com.hotel.entity.Incidencia;
import com.hotel.repository.EvidenciaRepository;
import com.hotel.repository.IncidenciaRepository;
import com.hotel.repository.ServicioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.ReporteOperacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReporteOperacionServiceImpl implements ReporteOperacionService {

    private final ServicioRepository servicioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final UsuarioActual usuarioActual;
    private final List<String> estadosFinalizados;

    public ReporteOperacionServiceImpl(
            ServicioRepository servicioRepository,
            IncidenciaRepository incidenciaRepository,
            EvidenciaRepository evidenciaRepository,
            UsuarioActual usuarioActual,
            @Value("${reporte.operacion.estados-finalizados:COMPLETADO,FINALIZADO,CANCELADO}") String estadosFinalizados) {
        this.servicioRepository = servicioRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.usuarioActual = usuarioActual;
        List<String> lista = Arrays.stream(estadosFinalizados.split(","))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .map(String::toUpperCase)
                .toList();
        this.estadosFinalizados = lista.isEmpty() ? List.of("__NINGUNO__") : lista;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteOperacionResponse obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("El parámetro 'fechaInicio' es obligatorio (formato yyyy-MM-dd).");
        }
        if (fechaFin == null) {
            throw new IllegalArgumentException("El parámetro 'fechaFin' es obligatorio (formato yyyy-MM-dd).");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }

        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.atTime(23, 59, 59);

        // US03: un propietario solo ve la operacion de sus propios alojamientos
        if (usuarioActual.esSoloPropietario()) {
            return reportePropietario(fechaInicio, fechaFin, desde, hasta);
        }

        Map<String, Long> serviciosPorEstado = aMapa(
                servicioRepository.contarPorEstado(desde, hasta), "SIN_ESTADO");

        long totalServicios = serviciosPorEstado.values().stream().mapToLong(Long::longValue).sum();
        long completa = servicioRepository.contarConEvidenciaCompleta(desde, hasta);

        List<ReporteOperacionResponse.ServicioAtrasado> atrasados = servicioRepository
                .buscarAtrasados(desde, hasta, LocalDateTime.now(), estadosFinalizados)
                .stream()
                .map(this::aAtrasado)
                .toList();

        Map<String, Long> incidenciasPorPrioridad = aMapa(
                incidenciaRepository.contarPorPrioridad(desde, hasta), "SIN_PRIORIDAD");

        return new ReporteOperacionResponse(
                fechaInicio,
                fechaFin,
                serviciosPorEstado,
                new ReporteOperacionResponse.Evidencia(completa, totalServicios - completa),
                new ReporteOperacionResponse.Atrasos(atrasados.size(), atrasados),
                incidenciasPorPrioridad);
    }

    private ReporteOperacionResponse.ServicioAtrasado aAtrasado(Servicio s) {
        return new ReporteOperacionResponse.ServicioAtrasado(
                s.getIdServicio(), s.getTipo(), s.getEstado(), s.getInicio(), s.getFin());
    }

    private Map<String, Long> aMapa(List<Object[]> filas, String valorVacio) {
        Map<String, Long> mapa = new TreeMap<>();
        for (Object[] fila : filas) {
            String clave = fila[0] == null ? valorVacio : fila[0].toString();
            mapa.merge(clave, ((Number) fila[1]).longValue(), Long::sum);
        }
        return mapa;
    }

    /** Mismos indicadores, calculados solo con los servicios e incidencias de los alojamientos del propietario. */
    private ReporteOperacionResponse reportePropietario(LocalDate fechaInicio, LocalDate fechaFin,
                                                       LocalDateTime desde, LocalDateTime hasta) {
        List<Servicio> servicios = servicioRepository.findByInicioBetweenOrderByInicioAsc(desde, hasta).stream()
                .filter(s -> usuarioActual.puedeVer(s.getAlojamiento()))
                .toList();

        Map<String, Long> serviciosPorEstado = new TreeMap<>();
        servicios.forEach(s -> serviciosPorEstado.merge(s.getEstado() == null ? "SIN_ESTADO" : s.getEstado(), 1L, Long::sum));

        long completa = servicios.stream()
                .filter(s -> !evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(s.getIdServicio()).isEmpty())
                .count();

        LocalDateTime ahora = LocalDateTime.now();
        List<ReporteOperacionResponse.ServicioAtrasado> atrasados = servicios.stream()
                .filter(s -> s.getFin() != null && s.getFin().isBefore(ahora))
                .filter(s -> s.getEstado() == null || !estadosFinalizados.contains(s.getEstado().toUpperCase()))
                .map(this::aAtrasado)
                .toList();

        Map<String, Long> incidenciasPorPrioridad = new TreeMap<>();
        for (Incidencia i : incidenciaRepository.findAll()) {
            if (!usuarioActual.puedeVer(i.getAlojamiento())) {
                continue;
            }
            LocalDateTime referencia = i.getServicio() != null ? i.getServicio().getInicio()
                    : i.getReserva() != null ? i.getReserva().getEntrada() : null;
            if (referencia != null && !referencia.isBefore(desde) && !referencia.isAfter(hasta)) {
                incidenciasPorPrioridad.merge(i.getPrioridad() == null ? "SIN_PRIORIDAD" : i.getPrioridad(), 1L, Long::sum);
            }
        }

        return new ReporteOperacionResponse(fechaInicio, fechaFin, serviciosPorEstado,
                new ReporteOperacionResponse.Evidencia(completa, servicios.size() - completa),
                new ReporteOperacionResponse.Atrasos(atrasados.size(), atrasados),
                incidenciasPorPrioridad);
    }
}
