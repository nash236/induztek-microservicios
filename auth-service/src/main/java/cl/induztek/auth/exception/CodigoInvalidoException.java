package cl.induztek.auth.exception;

public class CodigoInvalidoException extends RuntimeException {

    public CodigoInvalidoException() {
        super("Código de autorización incorrecto");
    }
}
