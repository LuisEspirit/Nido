package com.hotel.service.impl;

import com.hotel.entity.Alojamiento;
import com.hotel.entity.Incidencia;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.IncidenciaRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.repository.ServicioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.IncidenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA", "CRITICA");
    private static final Set<String> ESTADOS = Set.of("ABIERTA", "EN_PROCESO", "RESUELTA", "CERRADA");

    private final IncidenciaRepository incidenciaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioActual usuarioActual;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository, AlojamientoRepository alojamientoRepository,
                                 ReservaRepository reservaRepository, ServicioRepository servicioRepository,
                                 UsuarioActual usuarioActual) {
        this.incidenciaRepository = incidenciaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> findAll() {
        return buscar(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Incidencia> findById(Integer id) {
        Optional<Incidencia> incidencia = incidenciaRepository.findById(id);
        incidencia.ifPresent(i -> usuarioActual.verificarAlojamiento(i.getAlojamiento()));
        return incidencia;
    }

    /** Al registrarse, la incidencia queda ABIERTA (la puede reportar tambien el personal operativo). */
    @Override
    @Transactional
    public Incidencia save(Incidencia incidencia) {
        incidencia.setIdIncidencia(null);
        incidencia.setEstado("ABIERTA");
        prepararYValidar(incidencia);
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia update(Integer id, Incidencia incidencia) {
        Incidencia existing = obtener(id);
        existing.setCategoria(incidencia.getCategoria());
        existing.setPrioridad(incidencia.getPrioridad());
        existing.setDescripcion(incidencia.getDescripcion());
        existing.setEstado(incidencia.getEstado() == null ? existing.getEstado() : incidencia.getEstado());
        existing.setAlojamiento(incidencia.getAlojamiento());
        existing.setReserva(incidencia.getReserva());
        existing.setServicio(incidencia.getServicio());
        prepararYValidar(existing);
        return incidenciaRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        incidenciaRepository.delete(obtener(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> buscar(String estado, String prioridad, Integer idAlojamiento) {
        return incidenciaRepository.buscar(estado, prioridad, idAlojamiento).stream()
                .filter(i -> usuarioActual.puedeVer(i.getAlojamiento()))
                .toList();
    }

    @Override
    @Transactional
    public Incidencia cambiarEstado(Integer id, String estado) {
        Incidencia incidencia = obtener(id);
        incidencia.setEstado(validar(estado, ESTADOS, "Estado de incidencia"));
        return incidenciaRepository.save(incidencia);
    }

    private void prepararYValidar(Incidencia incidencia) {
        Integer idAlojamiento = incidencia.getAlojamiento().getIdAlojamiento();
        Alojamiento alojamiento = alojamientoRepository.findById(idAlojamiento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alojamiento", idAlojamiento));
        usuarioActual.verificarAlojamiento(alojamiento);
        incidencia.setAlojamiento(alojamiento);

        if (incidencia.getReserva() != null && incidencia.getReserva().getIdReserva() != null) {
            Integer idReserva = incidencia.getReserva().getIdReserva();
            incidencia.setReserva(reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", idReserva)));
            if (!incidencia.getReserva().getAlojamiento().getIdAlojamiento().equals(idAlojamiento)) {
                throw new ReglaNegocioException("La reserva " + idReserva + " no corresponde al alojamiento "
                        + idAlojamiento + ".", HttpStatus.BAD_REQUEST);
            }
        } else {
            incidencia.setReserva(null);
        }
        if (incidencia.getServicio() != null && incidencia.getServicio().getIdServicio() != null) {
            Integer idServicio = incidencia.getServicio().getIdServicio();
            incidencia.setServicio(servicioRepository.findById(idServicio)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", idServicio)));
        } else {
            incidencia.setServicio(null);
        }

        incidencia.setPrioridad(validar(incidencia.getPrioridad(), PRIORIDADES, "Prioridad"));
        incidencia.setEstado(validar(incidencia.getEstado(), ESTADOS, "Estado de incidencia"));
        incidencia.setCategoria(incidencia.getCategoria().trim().toUpperCase());
    }

    private String validar(String valor, Set<String> permitidos, String campo) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!permitidos.contains(normalizado)) {
            throw new ReglaNegocioException(campo + " invalido: " + valor + ". Valores permitidos: " + permitidos,
                    HttpStatus.BAD_REQUEST);
        }
        return normalizado;
    }

    private Incidencia obtener(Integer id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia", id));
        usuarioActual.verificarAlojamiento(incidencia.getAlojamiento());
        return incidencia;
    }
}
