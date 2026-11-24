package cl.induztek.protocolos.dto;

import java.util.List;

public record PruebaResponse(
        Long id,
        Long equipoId,
        String codigoEquipo,
        long fechaHora,
        String resultado,
        String estado,
        String observaciones,
        String registradoPor,
        String registradoPorNombre,
        String revisadoPor,
        String revisadoPorNombre,
        String motivoRechazo,
        List<MedicionDto> mediciones
) {
}
