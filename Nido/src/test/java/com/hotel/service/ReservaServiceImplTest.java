package com.hotel.service;

import com.hotel.entity.Alojamiento;
import com.hotel.entity.Huesped;
import com.hotel.entity.Reserva;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.HuespedRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de negocio de las reservas (US06, US07). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaServiceImplTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private HuespedRepository huespedRepository;
    @Mock private UsuarioActual usuarioActual;
    @InjectMocks private ReservaServiceImpl reservaService;

    private Alojamiento alojamiento;

    @BeforeEach
    void preparar() {
        alojamiento = new Alojamiento();
        alojamiento.setIdAlojamiento(1);
        alojamiento.setNombre("Departamento Miraflores Vista Mar");
        alojamiento.setPrecioBase(140.0);
        alojamiento.setEstado("DISPONIBLE");
        Huesped huesped = new Huesped();
        huesped.setIdHuesped(2);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamiento));
        when(huespedRepository.findById(2)).thenReturn(Optional.of(huesped));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Reserva nuevaReserva(String entrada, String salida) {
        Reserva r = new Reserva();
        r.setIdUsuario(2);
        r.setEntrada(LocalDateTime.parse(entrada));
        r.setSalida(LocalDateTime.parse(salida));
        Alojamiento a = new Alojamiento();
        a.setIdAlojamiento(1);
        r.setAlojamiento(a);
        Huesped h = new Huesped();
        h.setIdHuesped(2);
        r.setHuesped(h);
        return r;
    }

    @Test
    @DisplayName("CP09: rechaza una reserva que se cruza con otra del mismo alojamiento")
    void rechazaReservaSolapada() {
        Reserva existente = nuevaReserva("2026-10-10T14:00:00", "2026-10-13T11:00:00");
        existente.setIdReserva(1);
        when(reservaRepository.buscarSolapadas(eq(1), any(), any(), isNull())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> reservaService.save(nuevaReserva("2026-10-12T14:00:00", "2026-10-15T11:00:00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya esta reservado")
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza una reserva cuya salida no es posterior a la entrada")
    void rechazaFechasInvertidas() {
        assertThatThrownBy(() -> reservaService.save(nuevaReserva("2026-12-10T14:00:00", "2026-12-09T11:00:00")))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Un alojamiento INACTIVO no acepta reservas")
    void rechazaAlojamientoInactivo() {
        alojamiento.setEstado("INACTIVO");
        assertThatThrownBy(() -> reservaService.save(nuevaReserva("2026-12-10T14:00:00", "2026-12-12T11:00:00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("INACTIVO");
    }

    @Test
    @DisplayName("Sin precio, calcula noches x precio base y deja la reserva PENDIENTE en PEN")
    void calculaPrecioPorNoches() {
        Reserva guardada = reservaService.save(nuevaReserva("2026-11-10T14:00:00", "2026-11-13T11:00:00"));

        assertThat(guardada.getPrecio()).isEqualTo(420.0);
        assertThat(guardada.getEstado()).isEqualTo("PENDIENTE");
        assertThat(guardada.getMoneda()).isEqualTo("PEN");
        verify(usuarioActual).verificarRegistrante(2);
    }

    @Test
    @DisplayName("No se puede cancelar una reserva FINALIZADA")
    void noCancelaFinalizada() {
        Reserva finalizada = nuevaReserva("2026-09-25T14:00:00", "2026-09-30T11:00:00");
        finalizada.setIdReserva(5);
        finalizada.setEstado("FINALIZADA");
        when(reservaRepository.findById(5)).thenReturn(Optional.of(finalizada));

        assertThatThrownBy(() -> reservaService.cancelar(5)).isInstanceOf(ReglaNegocioException.class);
    }
}
