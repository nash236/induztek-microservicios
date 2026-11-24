package cl.induztek.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-z0-9_]+$", message = "solo minúsculas, números y guion bajo") String usuario,
        @NotBlank @Size(min = 6) String clave,
        @NotBlank String rol,
        String codigoAutorizacion,
        String empresa
) {
}
