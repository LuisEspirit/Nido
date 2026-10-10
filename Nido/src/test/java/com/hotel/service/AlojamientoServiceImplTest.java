package com.hotel.service;

import com.hotel.entity.Alojamiento;
import com.hotel.entity.Usuario;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.UsuarioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.impl.AlojamientoServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Un alojamiento no se puede registrar dos veces (US03). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlojamientoServiceImplTest {

    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioActual usuarioActual;
    @InjectMocks private AlojamientoServiceImpl alojamientoService;

    private Usuario propietario;

    @BeforeEach
    void preparar() {
        propietario = new Usuario();
        propietario.setIdusuario(2);
        when(usuarioActual.esSoloPropietario()).thenReturn(true);
        when(usuarioActual.usuario()).thenReturn(propietario);
        when(alojamientoRepository.buscarPorDireccion(anyString(), anyInt())).thenReturn(List.of());
        when(alojamientoRepository.buscarPorNombreDelPropietario(anyString(), anyInt(), anyInt())).thenReturn(List.of());
        when(alojamientoRepository.save(any(Alojamiento.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Alojamiento nuevo(String nombre, String direccion) {
        Alojamiento a = new Alojamiento();
        a.setIdUsuario(2);
        a.setNombre(nombre);
        a.setDireccion(direccion);
        return a;
    }

    private Alojamiento existente(int id) {
        Alojamiento a = new Alojamiento();
        a.setIdAlojamiento(id);
        return a;
    }

    @Test
    @DisplayName("Un alojamiento nuevo se guarda cuando no existe otro igual")
    void guardaAlojamientoNuevo() {
        Alojamiento guardado = alojamientoService.save(nuevo("Loft Barranco", "Jr. Domeyer 250, Barranco"));

        assertThat(guardado.getPropietario()).isSameAs(propietario);
        verify(alojamientoRepository).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("No se registra un alojamiento con una direccion que ya existe (409)")
    void rechazaDireccionRepetida() {
        when(alojamientoRepository.buscarPorDireccion(eq("Av. Larco 123"), anyInt())).thenReturn(List.of(existente(7)));

        assertThatThrownBy(() -> alojamientoService.save(nuevo("Otro nombre", "Av. Larco 123")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un alojamiento registrado en la direccion")
                .satisfies(e -> assertThat(((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Un propietario no puede repetir el nombre de uno de sus alojamientos (409)")
    void rechazaNombreRepetidoDelMismoPropietario() {
        when(alojamientoRepository.buscarPorNombreDelPropietario(eq("Casa Miraflores"), eq(2), anyInt()))
                .thenReturn(List.of(existente(3)));

        assertThatThrownBy(() -> alojamientoService.save(nuevo("Casa Miraflores", "Calle Nueva 1")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya tiene un alojamiento llamado");
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Al actualizar, el propio alojamiento no cuenta como duplicado")
    void actualizarNoChocaConSigoMismo() {
        Alojamiento actual = existente(5);
        actual.setPropietario(propietario);
        when(alojamientoRepository.findById(5)).thenReturn(Optional.of(actual));

        alojamientoService.update(5, nuevo("Loft Barranco", "Jr. Domeyer 250, Barranco"));

        verify(alojamientoRepository).buscarPorDireccion("Jr. Domeyer 250, Barranco", 5);
        verify(alojamientoRepository).save(actual);
    }

    @Test
    @DisplayName("Al actualizar no se puede usar la direccion de otro alojamiento (409)")
    void actualizarRechazaDireccionDeOtro() {
        Alojamiento actual = existente(5);
        actual.setPropietario(propietario);
        when(alojamientoRepository.findById(5)).thenReturn(Optional.of(actual));
        when(alojamientoRepository.buscarPorDireccion(eq("Av. Larco 123"), eq(5))).thenReturn(List.of(existente(1)));

        assertThatThrownBy(() -> alojamientoService.update(5, nuevo("Loft", "Av. Larco 123")))
                .isInstanceOf(ReglaNegocioException.class);
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }
}
