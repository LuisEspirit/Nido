package com.hotel.service;

import com.hotel.entity.Huesped;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.HuespedRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.impl.HuespedServiceImpl;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Un huesped no se puede registrar dos veces (US05). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HuespedServiceImplTest {

    @Mock private HuespedRepository huespedRepository;
    @Mock private UsuarioActual usuarioActual;
    @InjectMocks private HuespedServiceImpl huespedService;

    @BeforeEach
    void preparar() {
        when(huespedRepository.findByCorreoIgnoreCaseAndIdHuespedNot(anyString(), anyInt())).thenReturn(List.of());
        when(huespedRepository.findByNombresIgnoreCaseAndApellidosIgnoreCaseAndTelefonoAndIdHuespedNot(
                anyString(), anyString(), anyString(), anyInt())).thenReturn(List.of());
        when(huespedRepository.save(any(Huesped.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Huesped huesped(String correo, String telefono) {
        Huesped h = new Huesped();
        h.setIdUsuario(2);
        h.setNombres("Maria");
        h.setApellidos("Quispe Torres");
        h.setCorreo(correo);
        h.setTelefono(telefono);
        h.setConsentimiento(true);
        return h;
    }

    @Test
    @DisplayName("Un huesped nuevo se guarda cuando no existe otro igual")
    void guardaHuespedNuevo() {
        huespedService.save(huesped("maria@correo.com", "987654321"));

        verify(huespedRepository).save(any(Huesped.class));
    }

    @Test
    @DisplayName("No se registra un huesped con un correo que ya existe (409)")
    void rechazaCorreoRepetido() {
        Huesped existente = new Huesped();
        existente.setIdHuesped(4);
        when(huespedRepository.findByCorreoIgnoreCaseAndIdHuespedNot(eq("maria@correo.com"), anyInt()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> huespedService.save(huesped("maria@correo.com", "900000000")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un huesped registrado con el correo")
                .satisfies(e -> assertThat(((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(huespedRepository, never()).save(any(Huesped.class));
    }

    @Test
    @DisplayName("No se registra un huesped con los mismos nombres, apellidos y telefono (409)")
    void rechazaMismaPersonaConElMismoTelefono() {
        Huesped existente = new Huesped();
        existente.setIdHuesped(6);
        when(huespedRepository.findByNombresIgnoreCaseAndApellidosIgnoreCaseAndTelefonoAndIdHuespedNot(
                eq("Maria"), eq("Quispe Torres"), eq("987654321"), anyInt())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> huespedService.save(huesped("otro@correo.com", "987654321")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("mismos nombres, apellidos y telefono");
        verify(huespedRepository, never()).save(any(Huesped.class));
    }
}
