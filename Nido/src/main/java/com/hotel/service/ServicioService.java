package com.hotel.service;

import com.hotel.dto.ChecklistItem;
import com.hotel.entity.Servicio;
import java.util.List;

public interface ServicioService {
    List<Servicio> findAll();
    List<Servicio> misServicios();
    Servicio findById(Integer id);
    Servicio save(Servicio servicio);
    Servicio update(Integer id, Servicio servicio);
    void deleteById(Integer id);
    Servicio cambiarEstado(Integer id, String estado);
    Servicio actualizarChecklist(Integer id, List<ChecklistItem> items);

    /** Verifica que el usuario de la sesion pueda ver/operar el servicio y lo devuelve. */
    Servicio obtenerConAcceso(Integer id);
}
