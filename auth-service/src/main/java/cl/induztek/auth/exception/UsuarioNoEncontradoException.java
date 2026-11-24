package cl.induztek.auth.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String usuario) {
        super("No existe el usuario '" + usuario + "'");
    }
}
