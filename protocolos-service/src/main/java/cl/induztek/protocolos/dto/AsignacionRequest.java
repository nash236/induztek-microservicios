package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;

public record AsignacionRequest(
        @NotBlank String tecnicoUsuario,
        @NotBlank String tecnicoNombre,
        @NotBlank String asignadoPorNombre,
        String indicaciones
) {
}
