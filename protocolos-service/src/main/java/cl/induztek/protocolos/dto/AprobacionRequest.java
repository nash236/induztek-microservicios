package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;

public record AprobacionRequest(
        @NotBlank String revisadoPor,
        @NotBlank String revisadoPorNombre
) {
}
