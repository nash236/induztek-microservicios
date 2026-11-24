package cl.induztek.auth.exception;

public class UsuarioExistenteException extends RuntimeException {

    public UsuarioExistenteException(String usuario) {
        super("El usuario '" + usuario + "' ya existe");
    }
}
