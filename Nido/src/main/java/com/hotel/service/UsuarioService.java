package com.hotel.service;

import com.hotel.entity.Usuario;
import java.util.List;

public interface UsuarioService {
    List<Usuario> findAll();
    Usuario findById(Integer id);
    Usuario save(Usuario usuario);
    Usuario update(Integer id, Usuario usuario);
    Usuario cambiarEstado(Integer id, String estado);
    void deleteById(Integer id);

    // Personal operativo (US09)
    List<Usuario> listarPersonal(boolean soloActivos);
    Usuario registrarPersonal(Usuario usuario);
    Usuario cambiarEstadoPersonal(Integer id, String estado);
}
