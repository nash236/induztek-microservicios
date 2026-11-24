package cl.induztek.auth.dto;

import cl.induztek.auth.model.Usuario;

public record UsuarioResponse(
        Long id,
        String usuario,
        String nombre,
        String apellido,
        String correo,
        String rol,
        String empresa
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsuario(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getRol(), u.getEmpresa());
    }
}
