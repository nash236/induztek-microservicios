package cl.induztek.protocolos.service;

import cl.induztek.protocolos.dto.AsignacionRequest;
import cl.induztek.protocolos.dto.RechazoSolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudResponse;
import cl.induztek.protocolos.exception.AccesoDenegadoException;
import cl.induztek.protocolos.exception.EstadoInvalidoException;
import cl.induztek.protocolos.exception.RecursoNoEncontradoException;
import cl.induztek.protocolos.model.Solicitud;
import cl.induztek.protocolos.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudService {

    public static final String ROL_INGENIERO = "INGENIERO";
    public static final String ROL_TECNICO = "TECNICO";

    public static final String SOLICITADA = "SOLICITADA";
    public static final String ASIGNADA = "ASIGNADA";
    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String COMPLETADA = "COMPLETADA";
    public static final String RECHAZADA = "RECHAZADA";

    private final SolicitudRepository repositorio;

    public SolicitudService(SolicitudRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public SolicitudResponse crear(SolicitudRequest solicitud) {
        Solicitud s = new Solicitud();
        s.setClienteUsuario(solicitud.clienteUsuario().trim());
        s.setClienteNombre(solicitud.clienteNombre().trim());
        s.setInstalacion(solicitud.instalacion().trim());
        s.setEquipoCodigo(solicitud.equipoCodigo() == null ? "" : solicitud.equipoCodigo().trim());
        s.setTipoServicio(solicitud.tipoServicio().trim());
        s.setDescripcion(solicitud.descripcion().trim());
        s.setPrioridad("Urgente".equalsIgnoreCase(solicitud.prioridad()) ? "Urgente" : "Normal");
        s.setFechaCreacion(solicitud.fechaCreacion() != null ? solicitud.fechaCreacion() : System.currentTimeMillis());
        s.setEstado(SOLICITADA);
        return SolicitudResponse.from(repositorio.save(s));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(String instalacion, String tecnico) {
        List<Solicitud> lista;
        if (tecnico != null && !tecnico.isBlank()) {
            lista = repositorio.findByTecnicoUsuarioOrderByFechaCreacionDesc(tecnico.trim());
        } else if (instalacion != null && !instalacion.isBlank()) {
            lista = repositorio.findByInstalacionOrderByFechaCreacionDesc(instalacion.trim());
        } else {
            lista = repositorio.findAllByOrderByFechaCreacionDesc();
        }
        return lista.stream().map(SolicitudResponse::from).toList();
    }

    @Transactional
    public SolicitudResponse asignar(Long id, AsignacionRequest solicitud, String rol) {
        exigirRol(rol, ROL_INGENIERO);
        Solicitud s = buscar(id);
        if (!SOLICITADA.equals(s.getEstado())) {
            throw new EstadoInvalidoException("El pedido " + id + " ya fue atendido");
        }
        s.setEstado(ASIGNADA);
        s.setTecnicoUsuario(solicitud.tecnicoUsuario().trim());
        s.setTecnicoNombre(solicitud.tecnicoNombre().trim());
        s.setAsignadoPorNombre(solicitud.asignadoPorNombre().trim());
        s.setIndicaciones(solicitud.indicaciones() == null || solicitud.indicaciones().isBlank()
                ? null : solicitud.indicaciones().trim());
        return SolicitudResponse.from(repositorio.save(s));
    }

    @Transactional
    public SolicitudResponse rechazar(Long id, RechazoSolicitudRequest solicitud, String rol) {
        exigirRol(rol, ROL_INGENIERO);
        Solicitud s = buscar(id);
        if (!SOLICITADA.equals(s.getEstado())) {
            throw new EstadoInvalidoException("El pedido " + id + " ya fue atendido");
        }
        s.setEstado(RECHAZADA);
        s.setMotivoRechazo(solicitud.motivo().trim());
        return SolicitudResponse.from(repositorio.save(s));
    }

    @Transactional
    public SolicitudResponse iniciar(Long id, String rol, String usuario) {
        exigirRol(rol, ROL_TECNICO);
        Solicitud s = buscar(id);
        if (!ASIGNADA.equals(s.getEstado())) {
            throw new EstadoInvalidoException("El pedido " + id + " no está en estado asignado");
        }
        exigirTecnicoAsignado(s, usuario);
        s.setEstado(EN_PROCESO);
        return SolicitudResponse.from(repositorio.save(s));
    }

    @Transactional
    public SolicitudResponse finalizar(Long id, String rol, String usuario) {
        exigirRol(rol, ROL_TECNICO);
        Solicitud s = buscar(id);
        if (!EN_PROCESO.equals(s.getEstado())) {
            throw new EstadoInvalidoException("El pedido " + id + " no está en proceso");
        }
        exigirTecnicoAsignado(s, usuario);
        s.setEstado(COMPLETADA);
        return SolicitudResponse.from(repositorio.save(s));
    }

    private Solicitud buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido " + id));
    }

    private void exigirRol(String rol, String requerido) {
        if (rol == null || !requerido.equalsIgnoreCase(rol.trim())) {
            throw new AccesoDenegadoException("Esta acción corresponde al rol " + requerido);
        }
    }

    private void exigirTecnicoAsignado(Solicitud s, String usuario) {
        if (usuario == null || !usuario.trim().equalsIgnoreCase(s.getTecnicoUsuario())) {
            throw new AccesoDenegadoException("Este pedido está asignado a otro técnico");
        }
    }
}
