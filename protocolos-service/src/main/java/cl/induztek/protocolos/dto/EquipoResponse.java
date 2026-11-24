package cl.induztek.protocolos.dto;

import cl.induztek.protocolos.model.Equipo;

public record EquipoResponse(Long id, String codigo, String tipo, String instalacion) {

    public static EquipoResponse from(Equipo e) {
        return new EquipoResponse(e.getId(), e.getCodigo(), e.getTipo(), e.getInstalacion());
    }
}
