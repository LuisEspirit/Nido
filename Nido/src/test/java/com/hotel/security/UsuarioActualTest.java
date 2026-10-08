package com.hotel.security;

import com.hotel.entity.Usuario;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Auditoria de registro (idUsuario) y aislamiento por rol (US03). */
class UsuarioActualTest {

    private UsuarioActual usuarioActual;

    @BeforeEach
    void preparar() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario lucia = new Usuario();
        lucia.setIdusuario(2);
        lucia.setLogin("lmendoza");
        when(repo.findByLogin("lmendoza")).thenReturn(Optional.of(lucia));
        usuarioActual = new UsuarioActual(repo);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "lmendoza", null, List.of(new SimpleGrantedAuthority("ROLE_PROPIETARIO"))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Un registro sin idUsuario responde 400")
    void exigeIdUsuario() {
        assertThatThrownBy(() -> usuarioActual.verificarRegistrante(null))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Un registro a nombre de otro usuario responde 403")
    void rechazaIdUsuarioAjeno() {
        assertThatThrownBy(() -> usuarioActual.verificarRegistrante(3))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting(e -> ((ReglaNegocioException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Acepta el idUsuario del usuario que inicio sesion")
    void aceptaIdUsuarioPropio() {
        assertThatCode(() -> usuarioActual.verificarRegistrante(2)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Un propietario no es administrador ni personal")
    void detectaRoles() {
        assertThat(usuarioActual.esSoloPropietario()).isTrue();
        assertThat(usuarioActual.esAdmin()).isFalse();
        assertThat(usuarioActual.esSoloPersonal()).isFalse();
    }
}
