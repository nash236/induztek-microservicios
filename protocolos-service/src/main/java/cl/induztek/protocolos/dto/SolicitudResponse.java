package cl.induztek.protocolos.dto;

import cl.induztek.protocolos.model.Solicitud;

public record SolicitudResponse(
        Long id,
        String clienteUsuario,
        String clienteNombre,
        String instalacion,
        String equipoCodigo,
        String tipoServicio,
        String descripcion,
        String prioridad,
        long fechaCreacion,
        String estado,
        String tecnicoUsuario,
        String tecnicoNombre,
        String asignadoPorNombre,
        String indicaciones,
        String motivoRechazo
) {
    public static SolicitudResponse from(Solicitud s) {
        return new SolicitudResponse(
                s.getId(), s.getClienteUsuario(), s.getClienteNombre(), s.getInstalacion(),
                s.getEquipoCodigo(), s.getTipoServicio(), s.getDescripcion(), s.getPrioridad(),
                s.getFechaCreacion(), s.getEstado(), s.getTecnicoUsuario(), s.getTecnicoNombre(),
                s.getAsignadoPorNombre(), s.getIndicaciones(), s.getMotivoRechazo());
    }
}
