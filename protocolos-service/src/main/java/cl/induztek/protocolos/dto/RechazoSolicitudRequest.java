package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazoSolicitudRequest(
        @NotBlank @Size(min = 5, max = 500) String motivo
) {
}
