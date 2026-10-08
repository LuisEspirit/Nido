package com.hotel.service.impl;

import com.hotel.entity.Pago;
import com.hotel.entity.Reserva;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.PagoRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PagoServiceImpl implements PagoService {

    private static final Set<String> TIPOS = Set.of("INGRESO", "GASTO");
    private static final Set<String> ESTADOS = Set.of("PAGADO", "PENDIENTE", "ANULADO");

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioActual usuarioActual;

    public PagoServiceImpl(PagoRepository pagoRepository, ReservaRepository reservaRepository,
                           UsuarioActual usuarioActual) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAll().stream()
                .filter(p -> usuarioActual.puedeVer(p.getReserva().getAlojamiento()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Integer id) {
        Optional<Pago> pago = pagoRepository.findById(id);
        pago.ifPresent(p -> usuarioActual.verificarAlojamiento(p.getReserva().getAlojamiento()));
        return pago;
    }

    @Override
    @Transactional
    public Pago save(Pago pago) {
        pago.setIdPago(null);
        prepararYValidar(pago);
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago update(Integer id, Pago pago) {
        Pago existing = obtener(id);
        existing.setTipo(pago.getTipo());
        existing.setImporte(pago.getImporte());
        existing.setMoneda(pago.getMoneda());
        existing.setFecha(pago.getFecha() == null ? existing.getFecha() : pago.getFecha());
        existing.setEstado(pago.getEstado());
        existing.setReserva(pago.getReserva());
        prepararYValidar(existing);
        return pagoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        pagoRepository.delete(obtener(id));
    }

    /** El pago pertenece a una reserva existente; tipo INGRESO o GASTO y estado PAGADO, PENDIENTE o ANULADO. */
    private void prepararYValidar(Pago pago) {
        Integer idReserva = pago.getReserva().getIdReserva();
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", idReserva));
        usuarioActual.verificarAlojamiento(reserva.getAlojamiento());
        pago.setReserva(reserva);

        pago.setTipo(validar(pago.getTipo(), "INGRESO", TIPOS, "Tipo de pago"));
        pago.setEstado(validar(pago.getEstado(), "PAGADO", ESTADOS, "Estado de pago"));
        if (pago.getMoneda() == null || pago.getMoneda().isBlank()) {
            pago.setMoneda(reserva.getMoneda() == null ? "PEN" : reserva.getMoneda());
        }
        if (pago.getFecha() == null) {
            pago.setFecha(LocalDateTime.now());
        }
    }

    private String validar(String valor, String porDefecto, Set<String> permitidos, String campo) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        String normalizado = valor.trim().toUpperCase();
        if (!permitidos.contains(normalizado)) {
            throw new ReglaNegocioException(campo + " invalido: " + valor + ". Valores permitidos: " + permitidos,
                    HttpStatus.BAD_REQUEST);
        }
        return normalizado;
    }

    private Pago obtener(Integer id) {
        Pago pago = pagoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        usuarioActual.verificarAlojamiento(pago.getReserva().getAlojamiento());
        return pago;
    }
}
