package cl.induztek.protocolos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PruebaRequest(
        String observaciones,
        @NotBlank String registradoPor,
        @NotBlank String registradoPorNombre,
        @NotEmpty List<@Valid MedicionDto> mediciones,
        Long fechaHora
) {
}
