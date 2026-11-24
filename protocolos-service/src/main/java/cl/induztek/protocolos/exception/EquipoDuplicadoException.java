package cl.induztek.protocolos.exception;

public class EquipoDuplicadoException extends RuntimeException {

    public EquipoDuplicadoException(String codigo) {
        super("Ya existe un equipo con el código " + codigo);
    }
}
