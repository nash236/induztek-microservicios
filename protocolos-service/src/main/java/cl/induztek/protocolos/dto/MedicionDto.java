package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicionDto(
        @NotBlank String parametro,
        @NotNull Double valor,
        String unidad
) {
}
