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
        alojamiento.setIdAlojamiento(null);
        alojamiento.setPropietario(resolverPropietario(alojamiento.getPropietario()));
        if (alojamiento.getEstado() == null || alojamiento.getEstado().isBlank()) {
            alojamiento.setEstado("DISPONIBLE");
        }
        return alojamientoRepository.save(alojamiento);
    }

    @Override
    @Transactional
    public Alojamiento update(Integer id, Alojamiento alojamiento) {
        Alojamiento existing = obtener(id);
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
        if (!usuarioActual.esSoloPropietario() && alojamiento.getPropietario() != null) {
            existing.setPropietario(resolverPropietario(alojamiento.getPropietario()));
        }
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
