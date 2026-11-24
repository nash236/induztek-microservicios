package cl.induztek.auth.service;

import cl.induztek.auth.dto.LoginRequest;
import cl.induztek.auth.dto.RegistroRequest;
import cl.induztek.auth.dto.UsuarioResponse;
import cl.induztek.auth.exception.CodigoInvalidoException;
import cl.induztek.auth.exception.CredencialesInvalidasException;
import cl.induztek.auth.exception.UsuarioExistenteException;
import cl.induztek.auth.exception.UsuarioNoEncontradoException;
import cl.induztek.auth.model.Usuario;
import cl.induztek.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String ROL_TECNICO = "TECNICO";
    public static final String ROL_INGENIERO = "INGENIERO";
    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String CODIGO_INGENIERO = "INGE-2026";

    private final UsuarioRepository repositorio;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository repositorio, PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.encoder = encoder;
    }

    public UsuarioResponse registrar(RegistroRequest solicitud) {
        String rol = solicitud.rol().trim().toUpperCase();
        if (!ROL_TECNICO.equals(rol) && !ROL_INGENIERO.equals(rol) && !ROL_CLIENTE.equals(rol)) {
            throw new IllegalArgumentException("Rol inválido: use TECNICO, INGENIERO o CLIENTE");
        }
        String empresa = solicitud.empresa() == null ? "" : solicitud.empresa().trim();
        if (ROL_CLIENTE.equals(rol) && empresa.isEmpty()) {
            throw new IllegalArgumentException("Un cliente debe indicar su instalación");
        }

        String nombreUsuario = solicitud.usuario().trim().toLowerCase();
        if (repositorio.existsByUsuario(nombreUsuario)) {
            throw new UsuarioExistenteException(nombreUsuario);
        }
        if (ROL_INGENIERO.equals(rol) && !CODIGO_INGENIERO.equals(solicitud.codigoAutorizacion())) {
            throw new CodigoInvalidoException();
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(nombreUsuario);
        usuario.setClaveHash(encoder.encode(solicitud.clave()));
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellido(solicitud.apellido().trim());
        usuario.setCorreo(solicitud.correo().trim());
        usuario.setRol(rol);
        usuario.setEmpresa(ROL_CLIENTE.equals(rol) ? empresa : "");

        return UsuarioResponse.from(repositorio.save(usuario));
    }

    public UsuarioResponse iniciarSesion(LoginRequest solicitud) {
        Usuario usuario = repositorio.findByUsuario(solicitud.usuario().trim().toLowerCase())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!encoder.matches(solicitud.clave(), usuario.getClaveHash())) {
            throw new CredencialesInvalidasException();
        }
        return UsuarioResponse.from(usuario);
    }

    public UsuarioResponse buscar(String nombreUsuario) {
        Usuario usuario = repositorio.findByUsuario(nombreUsuario.trim().toLowerCase())
                .orElseThrow(() -> new UsuarioNoEncontradoException(nombreUsuario));
        return UsuarioResponse.from(usuario);
    }
}
