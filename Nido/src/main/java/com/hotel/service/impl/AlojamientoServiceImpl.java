package com.hotel.service.impl;

import com.hotel.entity.Alojamiento;
import com.hotel.entity.Usuario;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.UsuarioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.AlojamientoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlojamientoServiceImpl implements AlojamientoService {

    /** Id usado al registrar: ningun alojamiento existente tiene este valor. */
    private static final Integer SIN_ID = -1;

    private final AlojamientoRepository alojamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public AlojamientoServiceImpl(AlojamientoRepository alojamientoRepository, UsuarioRepository usuarioRepository,
                                  UsuarioActual usuarioActual) {
        this.alojamientoRepository = alojamientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActual = usuarioActual;
    }

    /** Un propietario solo ve sus alojamientos; el administrador ve todos (US03). */
    @Override
    @Transactional(readOnly = true)
    public List<Alojamiento> findAll() {
        if (usuarioActual.esSoloPropietario()) {
            return alojamientoRepository.findByPropietario_Idusuario(usuarioActual.usuario().getIdusuario());
        }
        return alojamientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alojamiento> findById(Integer id) {
        Optional<Alojamiento> alojamiento = alojamientoRepository.findById(id);
        alojamiento.ifPresent(usuarioActual::verificarAlojamiento);
        return alojamiento;
    }

    @Override
    @Transactional
    public Alojamiento save(Alojamiento alojamiento) {
        usuarioActual.verificarRegistrante(alojamiento.getIdUsuario());
        alojamiento.setIdAlojamiento(null);
        alojamiento.setPropietario(resolverPropietario(alojamiento.getPropietario()));
        validarSinDuplicados(alojamiento, alojamiento.getPropietario(), SIN_ID);
        if (alojamiento.getEstado() == null || alojamiento.getEstado().isBlank()) {
            alojamiento.setEstado("DISPONIBLE");
        }
        return alojamientoRepository.save(alojamiento);
    }

    @Override
    @Transactional
    public Alojamiento update(Integer id, Alojamiento alojamiento) {
        Alojamiento existing = obtener(id);
        // Se valida antes de modificar nada: el propietario final es el actual, salvo que el administrador lo cambie
        Usuario propietarioFinal = existing.getPropietario();
        if (!usuarioActual.esSoloPropietario() && alojamiento.getPropietario() != null) {
            propietarioFinal = resolverPropietario(alojamiento.getPropietario());
        }
        validarSinDuplicados(alojamiento, propietarioFinal, id);
        existing.setNombre(alojamiento.getNombre());
        existing.setDireccion(alojamiento.getDireccion());
        existing.setLatitud(alojamiento.getLatitud());
        existing.setLongitud(alojamiento.getLongitud());
        existing.setCapacidad(alojamiento.getCapacidad());
        existing.setPrecioBase(alojamiento.getPrecioBase());
        if (alojamiento.getEstado() != null) {
            existing.setEstado(alojamiento.getEstado());
        }
        // Un propietario no puede transferir su alojamiento; el administrador si puede
        existing.setPropietario(propietarioFinal);
        existing.setUbigeo(alojamiento.getUbigeo());
        return alojamientoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        alojamientoRepository.delete(obtener(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alojamiento> buscarPorEstado(String estado) {
        if (usuarioActual.esSoloPropietario()) {
            return alojamientoRepository.findByEstadoAndPropietario_Idusuario(estado,
                    usuarioActual.usuario().getIdusuario());
        }
        return alojamientoRepository.findByEstado(estado);
    }

    /** El propietario confirma las coordenadas sugeridas antes de guardarlas (US04). */
    @Override
    @Transactional
    public Alojamiento confirmarUbicacion(Integer id, Double latitud, Double longitud) {
        Alojamiento alojamiento = obtener(id);
        alojamiento.setLatitud(latitud);
        alojamiento.setLongitud(longitud);
        return alojamientoRepository.save(alojamiento);
    }

    /**
     * Un alojamiento no se puede registrar dos veces: la direccion es unica en todo el sistema y
     * un propietario no puede tener dos alojamientos con el mismo nombre. Si ya existe, responde 409.
     */
    private void validarSinDuplicados(Alojamiento alojamiento, Usuario propietario, Integer idActual) {
        if (alojamiento.getDireccion() != null && !alojamiento.getDireccion().isBlank()) {
            alojamientoRepository.buscarPorDireccion(alojamiento.getDireccion(), idActual).stream().findFirst()
                    .ifPresent(a -> {
                        throw new ReglaNegocioException("Ya existe un alojamiento registrado en la direccion '"
                                + alojamiento.getDireccion().trim() + "' (id " + a.getIdAlojamiento() + ").");
                    });
        }
        if (alojamiento.getNombre() != null && !alojamiento.getNombre().isBlank() && propietario != null) {
            alojamientoRepository.buscarPorNombreDelPropietario(alojamiento.getNombre(), propietario.getIdusuario(),
                    idActual).stream().findFirst().ifPresent(a -> {
                        throw new ReglaNegocioException("Ya tiene un alojamiento llamado '"
                                + alojamiento.getNombre().trim() + "' (id " + a.getIdAlojamiento() + ").");
                    });
        }
    }

    private Alojamiento obtener(Integer id) {
        Alojamiento alojamiento = alojamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alojamiento", id));
        usuarioActual.verificarAlojamiento(alojamiento);
        return alojamiento;
    }

    /**
     * Si registra un propietario, el alojamiento queda a su nombre.
     * Si registra el administrador, debe indicar un usuario con rol PROPIETARIO.
     */
    private Usuario resolverPropietario(Usuario enviado) {
        if (usuarioActual.esSoloPropietario()) {
            return usuarioActual.usuario();
        }
        if (enviado == null || enviado.getIdusuario() == null) {
            throw new ReglaNegocioException("Debe indicar el propietario del alojamiento.", HttpStatus.BAD_REQUEST);
        }
        Usuario propietario = usuarioRepository.findById(enviado.getIdusuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", enviado.getIdusuario()));
        boolean esPropietario = propietario.getRoles().stream()
                .anyMatch(r -> "PROPIETARIO".equalsIgnoreCase(r.getNombre()));
        if (!esPropietario) {
            throw new ReglaNegocioException("El usuario " + propietario.getIdusuario()
                    + " no tiene el rol PROPIETARIO.", HttpStatus.BAD_REQUEST);
        }
        return propietario;
    }
}
