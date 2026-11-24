package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{2,3}-\\d{3,6}$", message = "formato esperado: AA-0000") String codigo,
        @NotBlank String tipo,
        @NotBlank @Size(min = 3) String instalacion
) {
}
