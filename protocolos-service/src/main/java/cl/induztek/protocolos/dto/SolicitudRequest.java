package cl.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitudRequest(
        @NotBlank String clienteUsuario,
        @NotBlank String clienteNombre,
        @NotBlank String instalacion,
        String equipoCodigo,
        @NotBlank String tipoServicio,
        @NotBlank @Size(min = 10, max = 1000) String descripcion,
        String prioridad,
        Long fechaCreacion
) {
}
