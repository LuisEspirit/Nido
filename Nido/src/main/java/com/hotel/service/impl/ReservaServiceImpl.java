package com.hotel.service.impl;

import com.hotel.dto.DisponibilidadResponse;
import com.hotel.entity.Alojamiento;
import com.hotel.entity.Huesped;
import com.hotel.entity.Reserva;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.HuespedRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "CONFIRMADA", "EN_CURSO", "FINALIZADA", "CANCELADA");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final HuespedRepository huespedRepository;
    private final UsuarioActual usuarioActual;

    public ReservaServiceImpl(ReservaRepository reservaRepository, AlojamientoRepository alojamientoRepository,
                              HuespedRepository huespedRepository, UsuarioActual usuarioActual) {
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.huespedRepository = huespedRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> findAll() {
        return reservaRepository.findAll().stream()
                .filter(r -> usuarioActual.puedeVer(r.getAlojamiento()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> findById(Integer id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        reserva.ifPresent(r -> usuarioActual.verificarAlojamiento(r.getAlojamiento()));
        return reserva;
    }

    @Override
    @Transactional
    public Reserva save(Reserva reserva) {
        reserva.setIdReserva(null);
        prepararYValidar(reserva, null);
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva update(Integer id, Reserva reserva) {
        Reserva existing = obtener(id);
        existing.setEntrada(reserva.getEntrada());
        existing.setSalida(reserva.getSalida());
        existing.setCanal(reserva.getCanal());
        existing.setPrecio(reserva.getPrecio());
        existing.setMoneda(reserva.getMoneda());
        existing.setEstado(reserva.getEstado() == null ? existing.getEstado() : reserva.getEstado());
        existing.setAlojamiento(reserva.getAlojamiento());
        existing.setHuesped(reserva.getHuesped());
        prepararYValidar(existing, id);
        return reservaRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        reservaRepository.delete(obtener(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorAlojamiento(Integer idAlojamiento) {
        cargarAlojamiento(idAlojamiento);
        return reservaRepository.findByAlojamiento_IdAlojamientoOrderByEntradaAsc(idAlojamiento);
    }

    /** Cancelar libera las fechas: una reserva CANCELADA ya no cuenta para el solapamiento. */
    @Override
    @Transactional
    public Reserva cancelar(Integer id) {
        Reserva reserva = obtener(id);
        if ("FINALIZADA".equalsIgnoreCase(reserva.getEstado())) {
            throw new ReglaNegocioException("No se puede cancelar una reserva FINALIZADA.");
        }
        reserva.setEstado("CANCELADA");
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse consultarDisponibilidad(Integer idAlojamiento, LocalDateTime entrada,
                                                         LocalDateTime salida) {
        validarFechas(entrada, salida);
        cargarAlojamiento(idAlojamiento);
        List<DisponibilidadResponse.Conflicto> conflictos = reservaRepository
                .buscarSolapadas(idAlojamiento, entrada, salida, null).stream()
                .map(r -> new DisponibilidadResponse.Conflicto(r.getIdReserva(), r.getEntrada(), r.getSalida(),
                        r.getEstado()))
                .toList();
        return new DisponibilidadResponse(idAlojamiento, entrada, salida, conflictos.isEmpty(), conflictos);
    }

    /**
     * Reglas de una reserva (US07):
     * - la salida debe ser posterior a la entrada,
     * - el alojamiento y el huesped deben existir, y el alojamiento no puede estar INACTIVO,
     * - no puede cruzarse con otra reserva activa del mismo alojamiento.
     * Si no se envia precio, se calcula como noches x precio base del alojamiento.
     */
    private void prepararYValidar(Reserva reserva, Integer idReserva) {
        validarFechas(reserva.getEntrada(), reserva.getSalida());

        Alojamiento alojamiento = cargarAlojamiento(reserva.getAlojamiento().getIdAlojamiento());
        if ("INACTIVO".equalsIgnoreCase(alojamiento.getEstado())) {
            throw new ReglaNegocioException("El alojamiento '" + alojamiento.getNombre()
                    + "' esta INACTIVO y no acepta reservas.");
        }
        reserva.setAlojamiento(alojamiento);

        Integer idHuesped = reserva.getHuesped().getIdHuesped();
        Huesped huesped = huespedRepository.findById(idHuesped)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped", idHuesped));
        reserva.setHuesped(huesped);

        String estado = reserva.getEstado() == null ? "PENDIENTE" : reserva.getEstado().trim().toUpperCase();
        if (!ESTADOS.contains(estado)) {
            throw new ReglaNegocioException("Estado de reserva invalido: " + reserva.getEstado()
                    + ". Valores permitidos: " + ESTADOS, HttpStatus.BAD_REQUEST);
        }
        reserva.setEstado(estado);

        if (!"CANCELADA".equals(estado)) {
            List<Reserva> solapadas = reservaRepository.buscarSolapadas(alojamiento.getIdAlojamiento(),
                    reserva.getEntrada(), reserva.getSalida(), idReserva);
            if (!solapadas.isEmpty()) {
                Reserva otra = solapadas.get(0);
                throw new ReglaNegocioException("El alojamiento ya esta reservado del "
                        + otra.getEntrada().format(FORMATO) + " al " + otra.getSalida().format(FORMATO)
                        + " (reserva " + otra.getIdReserva() + "). Elija otras fechas.");
            }
        }

        if (reserva.getPrecio() == null && alojamiento.getPrecioBase() != null) {
            long noches = Math.max(1, ChronoUnit.DAYS.between(reserva.getEntrada().toLocalDate(),
                    reserva.getSalida().toLocalDate()));
            reserva.setPrecio(noches * alojamiento.getPrecioBase());
        }
        if (reserva.getMoneda() == null || reserva.getMoneda().isBlank()) {
            reserva.setMoneda("PEN");
        }
    }

    private void validarFechas(LocalDateTime entrada, LocalDateTime salida) {
        if (entrada == null || salida == null) {
            throw new ReglaNegocioException("Debe indicar la fecha de entrada y la de salida.", HttpStatus.BAD_REQUEST);
        }
        if (!salida.isAfter(entrada)) {
            throw new ReglaNegocioException("La fecha de salida debe ser posterior a la de entrada.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private Alojamiento cargarAlojamiento(Integer idAlojamiento) {
        Alojamiento alojamiento = alojamientoRepository.findById(idAlojamiento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alojamiento", idAlojamiento));
        usuarioActual.verificarAlojamiento(alojamiento);
        return alojamiento;
    }

    private Reserva obtener(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        usuarioActual.verificarAlojamiento(reserva.getAlojamiento());
        return reserva;
    }
}
