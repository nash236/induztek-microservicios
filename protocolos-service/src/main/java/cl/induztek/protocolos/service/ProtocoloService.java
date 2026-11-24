package cl.induztek.protocolos.service;

import cl.induztek.protocolos.dto.AprobacionRequest;
import cl.induztek.protocolos.dto.DatoResumen;
import cl.induztek.protocolos.dto.EquipoRequest;
import cl.induztek.protocolos.dto.EquipoResponse;
import cl.induztek.protocolos.dto.MedicionDto;
import cl.induztek.protocolos.dto.PruebaRequest;
import cl.induztek.protocolos.dto.PruebaResponse;
import cl.induztek.protocolos.dto.RechazoRequest;
import cl.induztek.protocolos.exception.AccesoDenegadoException;
import cl.induztek.protocolos.exception.EquipoDuplicadoException;
import cl.induztek.protocolos.exception.EstadoInvalidoException;
import cl.induztek.protocolos.exception.RecursoNoEncontradoException;
import cl.induztek.protocolos.model.Equipo;
import cl.induztek.protocolos.model.Medicion;
import cl.induztek.protocolos.model.Prueba;
import cl.induztek.protocolos.repository.EquipoRepository;
import cl.induztek.protocolos.repository.PruebaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProtocoloService {

    public static final String ROL_INGENIERO = "INGENIERO";
    public static final String ROL_TECNICO = "TECNICO";

    public static final String REGISTRADO_EN_TERRENO = "REGISTRADO_EN_TERRENO";
    public static final String FORMALIZADO = "FORMALIZADO";
    public static final String RECHAZADO = "RECHAZADO";

    public static final String RESULTADO_PENDIENTE = "Pendiente";
    public static final String RESULTADO_APROBADO = "Aprobado";
    public static final String RESULTADO_RECHAZADO = "Rechazado";

    private final EquipoRepository equipoRepository;
    private final PruebaRepository pruebaRepository;

    public ProtocoloService(EquipoRepository equipoRepository, PruebaRepository pruebaRepository) {
        this.equipoRepository = equipoRepository;
        this.pruebaRepository = pruebaRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listarEquipos(String instalacion) {
        List<Equipo> equipos = (instalacion == null || instalacion.isBlank())
                ? equipoRepository.findAllByOrderByCodigoAsc()
                : equipoRepository.findByInstalacionOrderByCodigoAsc(instalacion.trim());
        return equipos.stream()
                .map(EquipoResponse::from)
                .toList();
    }

    @Transactional
    public EquipoResponse crearEquipo(EquipoRequest solicitud, String rol) {
        exigirPersonal(rol);

        String codigo = solicitud.codigo().trim().toUpperCase();
        if (equipoRepository.existsByCodigo(codigo)) {
            throw new EquipoDuplicadoException(codigo);
        }

        Equipo equipo = new Equipo();
        equipo.setCodigo(codigo);
        equipo.setTipo(solicitud.tipo().trim());
        equipo.setInstalacion(solicitud.instalacion().trim());
        return EquipoResponse.from(equipoRepository.save(equipo));
    }

    @Transactional
    public PruebaResponse registrarPrueba(Long equipoId, PruebaRequest solicitud) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el equipo " + equipoId));

        Prueba prueba = new Prueba();
        prueba.setEquipo(equipo);
        prueba.setFechaHora(solicitud.fechaHora() != null ? solicitud.fechaHora() : System.currentTimeMillis());
        prueba.setResultado(RESULTADO_PENDIENTE);
        prueba.setEstado(REGISTRADO_EN_TERRENO);
        prueba.setObservaciones(solicitud.observaciones() == null ? "" : solicitud.observaciones().trim());
        prueba.setRegistradoPor(solicitud.registradoPor().trim());
        prueba.setRegistradoPorNombre(solicitud.registradoPorNombre().trim());

        for (MedicionDto dto : solicitud.mediciones()) {
            Medicion medicion = new Medicion();
            medicion.setPrueba(prueba);
            medicion.setParametro(dto.parametro().trim());
            medicion.setValor(dto.valor());
            medicion.setUnidad(dto.unidad() == null ? "" : dto.unidad().trim());
            prueba.getMediciones().add(medicion);
        }

        return aRespuesta(pruebaRepository.save(prueba));
    }

    @Transactional(readOnly = true)
    public List<PruebaResponse> listarPruebas(Long equipoId) {
        return pruebaRepository.findByEquipoIdOrderByFechaHoraDesc(equipoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PruebaResponse> listarTrabajosDeInstalacion(String instalacion) {
        return pruebaRepository.findByEquipoInstalacionOrderByFechaHoraDesc(instalacion.trim()).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PruebaResponse> listarPendientes() {
        return pruebaRepository.findByEstadoOrderByFechaHoraDesc(REGISTRADO_EN_TERRENO).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public PruebaResponse aprobar(Long pruebaId, AprobacionRequest solicitud, String rol) {
        exigirIngeniero(rol);
        Prueba prueba = buscarPendiente(pruebaId);

        prueba.setEstado(FORMALIZADO);
        prueba.setResultado(RESULTADO_APROBADO);
        prueba.setRevisadoPor(solicitud.revisadoPor().trim());
        prueba.setRevisadoPorNombre(solicitud.revisadoPorNombre().trim());
        prueba.setMotivoRechazo(null);
        return aRespuesta(pruebaRepository.save(prueba));
    }

    @Transactional
    public PruebaResponse rechazar(Long pruebaId, RechazoRequest solicitud, String rol) {
        exigirIngeniero(rol);
        Prueba prueba = buscarPendiente(pruebaId);

        prueba.setEstado(RECHAZADO);
        prueba.setResultado(RESULTADO_RECHAZADO);
        prueba.setRevisadoPor(solicitud.revisadoPor().trim());
        prueba.setRevisadoPorNombre(solicitud.revisadoPorNombre().trim());
        prueba.setMotivoRechazo(solicitud.motivo().trim());
        return aRespuesta(pruebaRepository.save(prueba));
    }

    @Transactional(readOnly = true)
    public List<DatoResumen> resumenPorResultado() {
        return pruebaRepository.contarPorResultado().stream()
                .map(fila -> new DatoResumen((String) fila[0], ((Number) fila[1]).longValue()))
                .toList();
    }

    private Prueba buscarPendiente(Long pruebaId) {
        Prueba prueba = pruebaRepository.findById(pruebaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la prueba " + pruebaId));
        if (!REGISTRADO_EN_TERRENO.equals(prueba.getEstado())) {
            throw new EstadoInvalidoException("La prueba " + pruebaId + " ya fue revisada");
        }
        return prueba;
    }

    private void exigirPersonal(String rol) {
        boolean permitido = rol != null
                && (ROL_INGENIERO.equalsIgnoreCase(rol.trim()) || ROL_TECNICO.equalsIgnoreCase(rol.trim()));
        if (!permitido) {
            throw new AccesoDenegadoException("Solo el personal de Induztek puede realizar esta acción");
        }
    }

    private void exigirIngeniero(String rol) {
        if (rol == null || !ROL_INGENIERO.equalsIgnoreCase(rol.trim())) {
            throw new AccesoDenegadoException("Solo un ingeniero puede realizar esta acción");
        }
    }

    private PruebaResponse aRespuesta(Prueba p) {
        List<MedicionDto> mediciones = p.getMediciones().stream()
                .map(m -> new MedicionDto(m.getParametro(), m.getValor(), m.getUnidad()))
                .toList();
        return new PruebaResponse(
                p.getId(),
                p.getEquipo().getId(),
                p.getEquipo().getCodigo(),
                p.getFechaHora(),
                p.getResultado(),
                p.getEstado(),
                p.getObservaciones(),
                p.getRegistradoPor(),
                p.getRegistradoPorNombre(),
                p.getRevisadoPor(),
                p.getRevisadoPorNombre(),
                p.getMotivoRechazo(),
                mediciones
        );
    }
}
