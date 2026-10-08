package com.hotel.service.impl;

import com.hotel.entity.Evidencia;
import com.hotel.entity.Servicio;
import com.hotel.exception.RecursoNoEncontradoException;
import com.hotel.exception.ReglaNegocioException;
import com.hotel.repository.EvidenciaRepository;
import com.hotel.security.UsuarioActual;
import com.hotel.service.EvidenciaService;
import com.hotel.service.ServicioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fotos de evidencia de los servicios (US12). Los archivos se guardan en disco y los datos en la tabla evidencia. */
@Service
public class EvidenciaServiceImpl implements EvidenciaService {

    /** Formatos de imagen aceptados y la extension con que se guardan. */
    private static final Map<String, String> TIPOS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final EvidenciaRepository evidenciaRepository;
    private final ServicioService servicioService;
    private final UsuarioActual usuarioActual;
    private final Path directorio;

    public EvidenciaServiceImpl(EvidenciaRepository evidenciaRepository, ServicioService servicioService,
                                UsuarioActual usuarioActual,
                                @Value("${nido.evidencias.directorio:uploads/evidencias}") String directorio) {
        this.evidenciaRepository = evidenciaRepository;
        this.servicioService = servicioService;
        this.usuarioActual = usuarioActual;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evidencia> listarPorServicio(Integer idServicio) {
        servicioService.obtenerConAcceso(idServicio);
        return evidenciaRepository.findByServicio_IdServicioOrderByFechaAsc(idServicio);
    }

    @Override
    @Transactional
    public Evidencia subir(Integer idServicio, MultipartFile archivo) {
        Servicio servicio = servicioService.obtenerConAcceso(idServicio);
        if ("COMPLETADO".equalsIgnoreCase(servicio.getEstado()) || "CANCELADO".equalsIgnoreCase(servicio.getEstado())) {
            throw new ReglaNegocioException("No se pueden agregar evidencias a un servicio " + servicio.getEstado() + ".");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Debe adjuntar una foto en el campo 'archivo'.", HttpStatus.BAD_REQUEST);
        }
        String extension = TIPOS.get(archivo.getContentType());
        if (extension == null) {
            throw new ReglaNegocioException("Formato no permitido (" + archivo.getContentType()
                    + "). Solo se aceptan fotos JPG, PNG o WEBP.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        // Nombre generado en el servidor: evita choques y rutas maliciosas en el nombre original
        Path destino = directorio.resolve(UUID.randomUUID() + extension);
        try (InputStream in = archivo.getInputStream()) {
            Files.createDirectories(directorio);
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo guardar la foto en el servidor.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Evidencia evidencia = new Evidencia();
        evidencia.setNombreArchivo(nombreSeguro(archivo.getOriginalFilename(), extension));
        evidencia.setRuta(destino.toString());
        evidencia.setTipoArchivo(archivo.getContentType());
        evidencia.setTamanio(archivo.getSize());
        evidencia.setFecha(LocalDateTime.now());
        evidencia.setServicio(servicio);
        evidencia.setUsuario(usuarioActual.usuario());
        return evidenciaRepository.save(evidencia);
    }

    @Override
    @Transactional(readOnly = true)
    public Evidencia findById(Integer id) {
        Evidencia evidencia = evidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evidencia", id));
        servicioService.obtenerConAcceso(evidencia.getServicio().getIdServicio());
        return evidencia;
    }

    @Override
    public Resource archivo(Evidencia evidencia) {
        // Las rutas relativas (como las del script de datos de prueba) se resuelven desde la carpeta de trabajo
        Path ruta = Path.of(evidencia.getRuta()).toAbsolutePath();
        if (!Files.isReadable(ruta)) {
            throw new RecursoNoEncontradoException("Archivo de la evidencia", evidencia.getIdEvidencia());
        }
        return new PathResource(ruta);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        Evidencia evidencia = findById(id);
        if ("COMPLETADO".equalsIgnoreCase(evidencia.getServicio().getEstado())) {
            throw new ReglaNegocioException("No se puede eliminar la evidencia de un servicio COMPLETADO.");
        }
        if (usuarioActual.esSoloPersonal() && (evidencia.getUsuario() == null
                || !evidencia.getUsuario().getLogin().equals(usuarioActual.login()))) {
            throw new ReglaNegocioException("Solo puede eliminar las evidencias que usted subio.", HttpStatus.FORBIDDEN);
        }
        evidenciaRepository.delete(evidencia);
        try {
            Files.deleteIfExists(Path.of(evidencia.getRuta()));
        } catch (IOException e) {
            // El registro ya se elimino; si el archivo no se pudo borrar no afecta al usuario
        }
    }

    private String nombreSeguro(String original, String extension) {
        if (original == null || original.isBlank()) {
            return "evidencia" + extension;
        }
        String nombre = Path.of(original).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
        return nombre.length() > 200 ? nombre.substring(nombre.length() - 200) : nombre;
    }
}
