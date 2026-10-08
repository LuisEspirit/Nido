package com.hotel.service;

import com.hotel.entity.Evidencia;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EvidenciaService {
    List<Evidencia> listarPorServicio(Integer idServicio);
    Evidencia subir(Integer idServicio, MultipartFile archivo);
    Evidencia findById(Integer id);
    Resource archivo(Evidencia evidencia);
    void deleteById(Integer id);
}
