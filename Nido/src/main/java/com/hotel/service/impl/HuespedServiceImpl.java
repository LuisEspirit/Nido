package com.hotel.service.impl;

import com.hotel.entity.Huesped;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.HuespedRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.HuespedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HuespedServiceImpl implements HuespedService {

    /** Id usado al registrar: ningun huesped existente tiene este valor. */
    private static final Integer SIN_ID = -1;

    private final HuespedRepository huespedRepository;
    private final UsuarioActual usuarioActual;

    public HuespedServiceImpl(HuespedRepository huespedRepository, UsuarioActual usuarioActual) {
        this.huespedRepository = huespedRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Huesped> findAll() {
        return huespedRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Huesped> findById(Integer id) {
        return huespedRepository.findById(id);
    }

    @Override
    @Transactional
    public Huesped save(Huesped huesped) {
        usuarioActual.verificarRegistrante(huesped.getIdUsuario());
        huesped.setIdHuesped(null);
        validarSinDuplicados(huesped, SIN_ID);
        return huespedRepository.save(huesped);
    }

    @Override
    @Transactional
    public Huesped update(Integer id, Huesped huesped) {
        return huespedRepository.findById(id).map(existing -> {
            validarSinDuplicados(huesped, id);
            existing.setNombres(huesped.getNombres());
            existing.setApellidos(huesped.getApellidos());
            existing.setCorreo(huesped.getCorreo());
            existing.setTelefono(huesped.getTelefono());
            existing.setConsentimiento(huesped.getConsentimiento());
            return huespedRepository.save(existing);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Huesped", id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        huespedRepository.delete(huespedRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped", id)));
    }

    /**
     * Un huesped no se puede registrar dos veces: no puede repetirse el correo, ni la combinacion
     * de nombres, apellidos y telefono. Si ya existe, responde 409.
     */
    private void validarSinDuplicados(Huesped huesped, Integer idActual) {
        String correo = huesped.getCorreo() == null ? "" : huesped.getCorreo().trim();
        if (!correo.isEmpty()) {
            huespedRepository.findByCorreoIgnoreCaseAndIdHuespedNot(correo, idActual).stream().findFirst()
                    .ifPresent(h -> {
                        throw new ReglaNegocioException("Ya existe un huesped registrado con el correo '"
                                + correo + "' (id " + h.getIdHuesped() + ").");
                    });
        }
        String telefono = huesped.getTelefono() == null ? "" : huesped.getTelefono().trim();
        if (!telefono.isEmpty() && huesped.getNombres() != null && huesped.getApellidos() != null) {
            huespedRepository.findByNombresIgnoreCaseAndApellidosIgnoreCaseAndTelefonoAndIdHuespedNot(
                    huesped.getNombres().trim(), huesped.getApellidos().trim(), telefono, idActual).stream()
                    .findFirst().ifPresent(h -> {
                        throw new ReglaNegocioException("Ya existe un huesped con los mismos nombres, apellidos y telefono (id "
                                + h.getIdHuesped() + ").");
                    });
        }
    }
}
