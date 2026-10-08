package com.hotel.service.impl;

import com.hotel.dto.CalendarioEvento;
import com.hotel.dto.ReporteIngresosResponse;
import com.hotel.dto.ReporteOcupacionResponse;
import com.hotel.entity.Alojamiento;
import com.hotel.entity.Pago;
import com.hotel.entity.Reserva;
import com.hotel.entity.Servicio;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.PagoRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.repository.ServicioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reportes de ingresos y ocupacion (US14) y calendario de reservas y servicios (US08). */
@Service
public class ReporteServiceImpl implements ReporteService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final UsuarioActual usuarioActual;

    public ReporteServiceImpl(PagoRepository pagoRepository, ReservaRepository reservaRepository,
                              ServicioRepository servicioRepository, AlojamientoRepository alojamientoRepository,
                              UsuarioActual usuarioActual) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteIngresosResponse ingresos(LocalDate fechaInicio, LocalDate fechaFin, String moneda) {
        validarPeriodo(fechaInicio, fechaFin);
        List<Pago> pagos = pagoRepository.buscarEnPeriodo(fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), moneda)
                .stream()
                .filter(p -> usuarioActual.puedeVer(p.getReserva().getAlojamiento()))
                .toList();

        double ingresos = 0;
        double gastos = 0;
        double pendiente = 0;
        Map<Integer, double[]> porAlojamiento = new LinkedHashMap<>();
        Map<Integer, String> nombres = new LinkedHashMap<>();
        for (Pago pago : pagos) {
            boolean esGasto = "GASTO".equalsIgnoreCase(pago.getTipo());
            if ("PENDIENTE".equalsIgnoreCase(pago.getEstado()) && !esGasto) {
                pendiente += pago.getImporte();
                continue;
            }
            if (!"PAGADO".equalsIgnoreCase(pago.getEstado())) {
                continue;
            }
            Alojamiento alojamiento = pago.getReserva().getAlojamiento();
            double[] montos = porAlojamiento.computeIfAbsent(alojamiento.getIdAlojamiento(), k -> new double[2]);
            nombres.put(alojamiento.getIdAlojamiento(), alojamiento.getNombre());
            if (esGasto) {
                gastos += pago.getImporte();
                montos[1] += pago.getImporte();
            } else {
                ingresos += pago.getImporte();
                montos[0] += pago.getImporte();
            }
        }

        List<ReporteIngresosResponse.PorAlojamiento> detalle = porAlojamiento.entrySet().stream()
                .map(e -> new ReporteIngresosResponse.PorAlojamiento(e.getKey(), nombres.get(e.getKey()),
                        redondear(e.getValue()[0]), redondear(e.getValue()[1]),
                        redondear(e.getValue()[0] - e.getValue()[1])))
                .sorted(Comparator.comparingDouble(ReporteIngresosResponse.PorAlojamiento::neto).reversed())
                .toList();

        return new ReporteIngresosResponse(fechaInicio, fechaFin, moneda.toUpperCase(), redondear(ingresos),
                redondear(gastos), redondear(ingresos - gastos), redondear(pendiente), detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteOcupacionResponse ocupacion(LocalDate fechaInicio, LocalDate fechaFin) {
        validarPeriodo(fechaInicio, fechaFin);
        // Noches del periodo: cada fecha desde fechaInicio hasta fechaFin (inclusive) es una noche posible
        long nochesPeriodo = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        List<Reserva> reservas = reservaRepository.buscarEnPeriodo(fechaInicio.atStartOfDay(),
                fechaFin.plusDays(1).atStartOfDay());

        List<ReporteOcupacionResponse.PorAlojamiento> detalle = new ArrayList<>();
        for (Alojamiento alojamiento : alojamientosVisibles()) {
            if ("INACTIVO".equalsIgnoreCase(alojamiento.getEstado())) {
                continue;
            }
            List<Reserva> propias = reservas.stream()
                    .filter(r -> r.getAlojamiento().getIdAlojamiento().equals(alojamiento.getIdAlojamiento()))
                    .toList();
            long noches = propias.stream().mapToLong(r -> nochesDentro(r, fechaInicio, fechaFin)).sum();
            noches = Math.min(noches, nochesPeriodo);
            detalle.add(new ReporteOcupacionResponse.PorAlojamiento(alojamiento.getIdAlojamiento(),
                    alojamiento.getNombre(), noches, propias.size(), porcentaje(noches, nochesPeriodo)));
        }
        detalle.sort(Comparator.comparingDouble(ReporteOcupacionResponse.PorAlojamiento::ocupacion).reversed());

        double promedio = detalle.isEmpty() ? 0
                : redondear(detalle.stream().mapToDouble(ReporteOcupacionResponse.PorAlojamiento::ocupacion)
                        .average().orElse(0));
        return new ReporteOcupacionResponse(fechaInicio, fechaFin, nochesPeriodo, promedio, detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioEvento> calendario(LocalDate fechaInicio, LocalDate fechaFin, Integer idAlojamiento) {
        validarPeriodo(fechaInicio, fechaFin);
        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.plusDays(1).atStartOfDay();
        List<CalendarioEvento> eventos = new ArrayList<>();

        for (Reserva r : reservaRepository.buscarEnPeriodo(desde, hasta)) {
            if (incluir(r.getAlojamiento(), idAlojamiento)) {
                eventos.add(new CalendarioEvento("RESERVA", r.getIdReserva(), r.getAlojamiento().getIdAlojamiento(),
                        r.getAlojamiento().getNombre(), r.getEntrada(), r.getSalida(), r.getEstado(),
                        "Huesped: " + r.getHuesped().getNombres() + " " + r.getHuesped().getApellidos()
                                + (r.getCanal() == null ? "" : " (" + r.getCanal() + ")")));
            }
        }
        for (Servicio s : servicioRepository.findByInicioBetweenOrderByInicioAsc(desde, hasta.minusSeconds(1))) {
            if (incluir(s.getAlojamiento(), idAlojamiento)) {
                eventos.add(new CalendarioEvento("SERVICIO", s.getIdServicio(), s.getAlojamiento().getIdAlojamiento(),
                        s.getAlojamiento().getNombre(), s.getInicio(), s.getFin(), s.getEstado(),
                        s.getTipo() + " - " + s.getUsuario().getNombres() + " "
                                + (s.getUsuario().getApellidos() == null ? "" : s.getUsuario().getApellidos())));
            }
        }
        eventos.sort(Comparator.comparing(CalendarioEvento::inicio));
        return eventos;
    }

    private boolean incluir(Alojamiento alojamiento, Integer idAlojamiento) {
        return usuarioActual.puedeVer(alojamiento)
                && (idAlojamiento == null || alojamiento.getIdAlojamiento().equals(idAlojamiento));
    }

    private List<Alojamiento> alojamientosVisibles() {
        if (usuarioActual.esSoloPropietario()) {
            return alojamientoRepository.findByPropietario_Idusuario(usuarioActual.usuario().getIdusuario());
        }
        return alojamientoRepository.findAll();
    }

    /** Noches de la reserva que caen dentro del periodo [inicio, fin]. */
    private long nochesDentro(Reserva r, LocalDate inicio, LocalDate fin) {
        LocalDate desde = r.getEntrada().toLocalDate().isBefore(inicio) ? inicio : r.getEntrada().toLocalDate();
        LocalDate salida = r.getSalida().toLocalDate();
        LocalDate hasta = salida.isAfter(fin.plusDays(1)) ? fin.plusDays(1) : salida;
        return Math.max(0, ChronoUnit.DAYS.between(desde, hasta));
    }

    private void validarPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }
        if (ChronoUnit.DAYS.between(fechaInicio, fechaFin) > 366) {
            throw new IllegalArgumentException("El periodo del reporte no puede superar un anio.");
        }
    }

    private double porcentaje(long parte, long total) {
        return total == 0 ? 0 : redondear(parte * 100.0 / total);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
