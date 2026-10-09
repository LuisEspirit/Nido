package com.hotel.service.impl;

import com.hotel.dto.ChecklistItem;
import com.hotel.entity.Alojamiento;
import com.hotel.entity.Reserva;
import com.hotel.entity.Servicio;
import com.hotel.entity.Usuario;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.EvidenciaRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.repository.ServicioRepository;
import com.hotel.repository.UsuarioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.ServicioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ServicioServiceImpl implements ServicioService {

    private static final Set<String> TIPOS = Set.of("LIMPIEZA", "MANTENIMIENTO", "INSPECCION");

    /**
     * Ciclo de vida de un servicio (US10). Cada estado indica a cuales puede pasar.
     * RECHAZADO puede volver a ASIGNADO cuando se reasigna a otra persona.
     */
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            "PENDIENTE", Set.of("ASIGNADO", "CANCELADO"),
            "ASIGNADO", Set.of("ACEPTADO", "RECHAZADO", "CANCELADO"),
            "ACEPTADO", Set.of("EN_PROCESO", "CANCELADO"),
            "EN_PROCESO", Set.of("COMPLETADO", "CANCELADO"),
            "RECHAZADO", Set.of("ASIGNADO", "CANCELADO"),
            "COMPLETADO", Set.of(),
            "CANCELADO", Set.of());

    /** Estados que el personal operativo puede marcar sobre sus propios servicios. */
    private static final Set<String> ESTADOS_PERSONAL = Set.of("ACEPTADO", "RECHAZADO", "EN_PROCESO", "COMPLETADO");

    /** Checklist sugerido cuando se crea un servicio sin checklist. */
    private static final Map<String, List<ChecklistItem>> CHECKLIST_BASE = Map.of(
            "LIMPIEZA", List.of(
                    new ChecklistItem("Cambiar sabanas y toallas", true, false),
                    new ChecklistItem("Limpiar bano y cocina", true, false),
                    new ChecklistItem("Barrer y trapear pisos", true, false),
                    new ChecklistItem("Reponer amenities", false, false)),
            "MANTENIMIENTO", List.of(
                    new ChecklistItem("Diagnosticar el problema", true, false),
                    new ChecklistItem("Realizar la reparacion", true, false),
                    new ChecklistItem("Probar el funcionamiento", true, false)),
            "INSPECCION", List.of(
                    new ChecklistItem("Revisar inventario", true, false),
                    new ChecklistItem("Revisar danos visibles", true, false),
                    new ChecklistItem("Verificar llaves y cerraduras", true, false)));

    private final ServicioRepository servicioRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final UsuarioActual usuarioActual;
    private final ObjectMapper objectMapper;

    public ServicioServiceImpl(ServicioRepository servicioRepository, AlojamientoRepository alojamientoRepository,
                               ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                               EvidenciaRepository evidenciaRepository, UsuarioActual usuarioActual,
                               ObjectMapper objectMapper) {
        this.servicioRepository = servicioRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.usuarioActual = usuarioActual;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> findAll() {
        if (usuarioActual.esSoloPersonal()) {
            return misServicios();
        }
        return servicioRepository.findAll().stream()
                .filter(s -> usuarioActual.puedeVer(s.getAlojamiento()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> misServicios() {
        return servicioRepository.findByPersonal_IdusuarioOrderByInicioAsc(usuarioActual.usuario().getIdusuario());
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio findById(Integer id) {
        return obtenerConAcceso(id);
    }

    @Override
    @Transactional
    public Servicio save(Servicio servicio) {
        usuarioActual.verificarRegistrante(servicio.getIdUsuario());
        servicio.setIdServicio(null);
        servicio.setEstado("ASIGNADO");
        prepararYValidar(servicio);
        if (servicio.getChecklist() == null || servicio.getChecklist().isBlank()) {
            servicio.setChecklist(escribir(CHECKLIST_BASE.get(servicio.getTipo())));
        }
        return servicioRepository.save(servicio);
    }

    @Override
    @Transactional
    public Servicio update(Integer id, Servicio servicio) {
        Servicio existing = obtenerConAcceso(id);
        if (esFinal(existing.getEstado())) {
            throw new ReglaNegocioException("No se puede modificar un servicio " + existing.getEstado() + ".");
        }
        boolean cambiaPersonal = servicio.getPersonal() != null
                && !servicio.getPersonal().getIdusuario().equals(existing.getPersonal().getIdusuario());
        existing.setTipo(servicio.getTipo());
        existing.setInicio(servicio.getInicio());
        existing.setFin(servicio.getFin());
        existing.setAlojamiento(servicio.getAlojamiento());
        existing.setReserva(servicio.getReserva());
        existing.setPersonal(servicio.getPersonal());
        if (servicio.getChecklist() != null && !servicio.getChecklist().isBlank()) {
            existing.setChecklist(servicio.getChecklist());
        }
        // Reasignar a otra persona reinicia el flujo: vuelve a quedar ASIGNADO
        if (cambiaPersonal) {
            existing.setEstado("ASIGNADO");
        }
        prepararYValidar(existing);
        return servicioRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        Servicio servicio = obtenerConAcceso(id);
        if (!evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(id).isEmpty()) {
            throw new ReglaNegocioException("El servicio tiene evidencias registradas; cancelelo en lugar de eliminarlo.");
        }
        servicioRepository.delete(servicio);
    }

    @Override
    @Transactional
    public Servicio cambiarEstado(Integer id, String estado) {
        Servicio servicio = obtenerConAcceso(id);
        String actual = servicio.getEstado() == null ? "PENDIENTE" : servicio.getEstado().toUpperCase();
        String nuevo = estado.trim().toUpperCase();

        if (!TRANSICIONES.containsKey(nuevo)) {
            throw new ReglaNegocioException("Estado invalido: " + estado + ". Valores permitidos: "
                    + TRANSICIONES.keySet(), HttpStatus.BAD_REQUEST);
        }
        if (usuarioActual.esSoloPersonal() && !ESTADOS_PERSONAL.contains(nuevo)) {
            throw new ReglaNegocioException("El personal operativo solo puede marcar " + ESTADOS_PERSONAL + ".",
                    HttpStatus.FORBIDDEN);
        }
        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar el servicio de " + actual + " a " + nuevo
                    + ". Desde " + actual + " se permite: " + TRANSICIONES.getOrDefault(actual, Set.of()) + ".");
        }
        if ("COMPLETADO".equals(nuevo)) {
            validarCierre(servicio);
        }
        servicio.setEstado(nuevo);
        return servicioRepository.save(servicio);
    }

    /**
     * Personal operativo: solo marca como hecho/no hecho los items existentes.
     * Administrador o propietario: reemplaza el checklist completo.
     */
    @Override
    @Transactional
    public Servicio actualizarChecklist(Integer id, List<ChecklistItem> items) {
        Servicio servicio = obtenerConAcceso(id);
        if (esFinal(servicio.getEstado())) {
            throw new ReglaNegocioException("El checklist de un servicio " + servicio.getEstado()
                    + " ya no se puede modificar.");
        }
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("El checklist no puede estar vacio.", HttpStatus.BAD_REQUEST);
        }
        for (ChecklistItem item : items) {
            if (item.item() == null || item.item().isBlank()) {
                throw new ReglaNegocioException("Cada item del checklist debe tener descripcion.", HttpStatus.BAD_REQUEST);
            }
        }

        List<ChecklistItem> resultado;
        if (usuarioActual.esSoloPersonal()) {
            List<ChecklistItem> actuales = leer(servicio.getChecklist());
            resultado = new ArrayList<>();
            for (ChecklistItem actual : actuales) {
                Boolean hecho = items.stream()
                        .filter(i -> i.item().trim().equalsIgnoreCase(actual.item()))
                        .map(ChecklistItem::hecho)
                        .findFirst()
                        .orElse(actual.hecho());
                resultado.add(new ChecklistItem(actual.item(), actual.obligatorio(), Boolean.TRUE.equals(hecho)));
            }
            for (ChecklistItem enviado : items) {
                boolean existe = actuales.stream().anyMatch(a -> a.item().equalsIgnoreCase(enviado.item().trim()));
                if (!existe) {
                    throw new ReglaNegocioException("El item '" + enviado.item() + "' no existe en el checklist.",
                            HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            resultado = items.stream()
                    .map(i -> new ChecklistItem(i.item().trim(), Boolean.TRUE.equals(i.obligatorio()),
                            Boolean.TRUE.equals(i.hecho())))
                    .toList();
        }
        servicio.setChecklist(escribir(resultado));
        return servicioRepository.save(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio obtenerConAcceso(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        if (usuarioActual.esSoloPersonal()) {
            if (!servicio.getPersonal().getLogin().equals(usuarioActual.login())) {
                throw new ReglaNegocioException("El servicio no esta asignado a usted.", HttpStatus.FORBIDDEN);
            }
        } else {
            usuarioActual.verificarAlojamiento(servicio.getAlojamiento());
        }
        return servicio;
    }

    /** Un servicio solo se cierra con todos los items obligatorios hechos (US11) y al menos una foto (US12). */
    private void validarCierre(Servicio servicio) {
        List<String> pendientes = leer(servicio.getChecklist()).stream()
                .filter(i -> Boolean.TRUE.equals(i.obligatorio()) && !Boolean.TRUE.equals(i.hecho()))
                .map(ChecklistItem::item)
                .toList();
        if (!pendientes.isEmpty()) {
            throw new ReglaNegocioException("No se puede completar el servicio: faltan items obligatorios del checklist "
                    + pendientes + ".");
        }
        if (evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(servicio.getIdServicio()).isEmpty()) {
            throw new ReglaNegocioException("No se puede completar el servicio: debe subir al menos una foto de evidencia.");
        }
    }

    private void prepararYValidar(Servicio servicio) {
        String tipo = servicio.getTipo().trim().toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new ReglaNegocioException("Tipo de servicio invalido: " + servicio.getTipo()
                    + ". Valores permitidos: " + TIPOS, HttpStatus.BAD_REQUEST);
        }
        servicio.setTipo(tipo);
        if (servicio.getFin() != null && !servicio.getFin().isAfter(servicio.getInicio())) {
            throw new ReglaNegocioException("La fecha de fin debe ser posterior a la de inicio.", HttpStatus.BAD_REQUEST);
        }

        Integer idAlojamiento = servicio.getAlojamiento().getIdAlojamiento();
        Alojamiento alojamiento = alojamientoRepository.findById(idAlojamiento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alojamiento", idAlojamiento));
        usuarioActual.verificarAlojamiento(alojamiento);
        servicio.setAlojamiento(alojamiento);

        if (servicio.getReserva() != null && servicio.getReserva().getIdReserva() != null) {
            Integer idReserva = servicio.getReserva().getIdReserva();
            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", idReserva));
            if (!reserva.getAlojamiento().getIdAlojamiento().equals(idAlojamiento)) {
                throw new ReglaNegocioException("La reserva " + idReserva + " no corresponde al alojamiento "
                        + idAlojamiento + ".", HttpStatus.BAD_REQUEST);
            }
            servicio.setReserva(reserva);
        } else {
            servicio.setReserva(null);
        }

        // Solo se asigna personal operativo ACTIVO (US09)
        Integer idUsuario = servicio.getPersonal().getIdusuario();
        Usuario personal = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", idUsuario));
        boolean esPersonal = personal.getRoles().stream().anyMatch(r -> "PERSONAL".equalsIgnoreCase(r.getNombre()));
        if (!esPersonal) {
            throw new ReglaNegocioException("El usuario " + idUsuario + " no pertenece al personal operativo.",
                    HttpStatus.BAD_REQUEST);
        }
        if (!"ACTIVO".equalsIgnoreCase(personal.getEstado())) {
            throw new ReglaNegocioException("No se puede asignar el servicio: " + personal.getNombres()
                    + " esta INACTIVO.");
        }
        servicio.setPersonal(personal);

        if (servicio.getChecklist() != null && !servicio.getChecklist().isBlank()) {
            servicio.setChecklist(escribir(leer(servicio.getChecklist())));
        }
    }

    private boolean esFinal(String estado) {
        return "COMPLETADO".equalsIgnoreCase(estado) || "CANCELADO".equalsIgnoreCase(estado);
    }

    private List<ChecklistItem> leer(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChecklistItem>>() { });
        } catch (JacksonException e) {
            throw new ReglaNegocioException("El checklist debe ser una lista JSON de items "
                    + "{\"item\", \"obligatorio\", \"hecho\"}.", HttpStatus.BAD_REQUEST);
        }
    }

    private String escribir(List<ChecklistItem> items) {
        return objectMapper.writeValueAsString(items);
    }
}
