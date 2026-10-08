package com.hotel.service;

import com.hotel.entity.Alojamiento;
import com.hotel.entity.Evidencia;
import com.hotel.entity.Rol;
import com.hotel.entity.Servicio;
import com.hotel.entity.Usuario;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.AlojamientoRepository;
import com.hotel.repository.EvidenciaRepository;
import com.hotel.repository.ReservaRepository;
import com.hotel.repository.ServicioRepository;
import com.hotel.repository.UsuarioRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.impl.ServicioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Ciclo de vida del servicio (US10), checklist obligatorio (US11) y evidencias (US12). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicioServiceImplTest {

    private static final String CHECKLIST_PENDIENTE =
            "[{\"item\":\"Cambiar sabanas y toallas\",\"obligatorio\":true,\"hecho\":false},"
            + "{\"item\":\"Reponer amenities\",\"obligatorio\":false,\"hecho\":false}]";
    private static final String CHECKLIST_HECHO =
            "[{\"item\":\"Cambiar sabanas y toallas\",\"obligatorio\":true,\"hecho\":true},"
            + "{\"item\":\"Reponer amenities\",\"obligatorio\":false,\"hecho\":false}]";

    @Mock private ServicioRepository servicioRepository;
    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EvidenciaRepository evidenciaRepository;
    @Mock private UsuarioActual usuarioActual;

    private ServicioServiceImpl servicioService;
    private Servicio servicio;

    @BeforeEach
    void preparar() {
        servicioService = new ServicioServiceImpl(servicioRepository, alojamientoRepository, reservaRepository,
                usuarioRepository, evidenciaRepository, usuarioActual, JsonMapper.builder().build());
        Usuario rosa = personal(4, "rhuaman", "ACTIVO");
        servicio = new Servicio();
        servicio.setIdServicio(20);
        servicio.setTipo("LIMPIEZA");
        servicio.setInicio(LocalDateTime.parse("2026-10-13T12:00:00"));
        servicio.setEstado("EN_PROCESO");
        servicio.setChecklist(CHECKLIST_PENDIENTE);
        servicio.setAlojamiento(new Alojamiento());
        servicio.setPersonal(rosa);
        when(servicioRepository.findById(20)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioActual.login()).thenReturn("rhuaman");
    }

    private static Usuario personal(int id, String login, String estado) {
        Rol rol = new Rol();
        rol.setNombre("PERSONAL");
        Usuario u = new Usuario();
        u.setIdusuario(id);
        u.setLogin(login);
        u.setNombres(login);
        u.setEstado(estado);
        u.setRoles(List.of(rol));
        return u;
    }

    @Test
    @DisplayName("US11: no completa el servicio con items obligatorios pendientes")
    void noCompletaConChecklistPendiente() {
        assertThatThrownBy(() -> servicioService.cambiarEstado(20, "COMPLETADO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Cambiar sabanas y toallas");
    }

    @Test
    @DisplayName("US12: no completa el servicio sin al menos una foto de evidencia")
    void noCompletaSinEvidencia() {
        servicio.setChecklist(CHECKLIST_HECHO);
        when(evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(20)).thenReturn(List.of());

        assertThatThrownBy(() -> servicioService.cambiarEstado(20, "COMPLETADO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("foto");
    }

    @Test
    @DisplayName("Completa el servicio con checklist obligatorio hecho y una foto")
    void completaConChecklistYEvidencia() {
        servicio.setChecklist(CHECKLIST_HECHO);
        when(evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(20)).thenReturn(List.of(new Evidencia()));

        assertThat(servicioService.cambiarEstado(20, "COMPLETADO").getEstado()).isEqualTo("COMPLETADO");
    }

    @Test
    @DisplayName("US10: no permite saltar de ASIGNADO a COMPLETADO")
    void rechazaTransicionInvalida() {
        servicio.setEstado("ASIGNADO");
        assertThatThrownBy(() -> servicioService.cambiarEstado(20, "COMPLETADO"))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("El personal operativo no puede cancelar servicios")
    void personalNoCancela() {
        when(usuarioActual.esSoloPersonal()).thenReturn(true);
        servicio.setEstado("ASIGNADO");
        assertThatThrownBy(() -> servicioService.cambiarEstado(20, "CANCELADO"))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("US09: no asigna servicios a personal INACTIVO")
    void noAsignaPersonalInactivo() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setIdAlojamiento(1);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamiento));
        when(usuarioRepository.findById(6)).thenReturn(Optional.of(personal(6, "mtorres", "INACTIVO")));

        Servicio nuevo = new Servicio();
        nuevo.setIdUsuario(2);
        nuevo.setTipo("LIMPIEZA");
        nuevo.setInicio(LocalDateTime.parse("2026-10-15T12:00:00"));
        Alojamiento ref = new Alojamiento();
        ref.setIdAlojamiento(1);
        nuevo.setAlojamiento(ref);
        Usuario ref6 = new Usuario();
        ref6.setIdusuario(6);
        nuevo.setPersonal(ref6);

        assertThatThrownBy(() -> servicioService.save(nuevo))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("INACTIVO");
    }
}
