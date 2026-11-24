package cl.induztek.auth.config;

import cl.induztek.auth.dto.RegistroRequest;
import cl.induztek.auth.repository.UsuarioRepository;
import cl.induztek.auth.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repositorio;
    private final AuthService servicio;

    public DatosIniciales(UsuarioRepository repositorio, AuthService servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) {
            return;
        }
        servicio.registrar(new RegistroRequest(
                "Carlos", "Soto", "carlos.soto@induztek.cl", "tecnico", "1234", "TECNICO", null, null));
        servicio.registrar(new RegistroRequest(
                "Marcela", "Rojas", "marcela.rojas@induztek.cl", "ingeniero", "1234", "INGENIERO",
                AuthService.CODIGO_INGENIERO, null));
        servicio.registrar(new RegistroRequest(
                "Constanza", "Vega", "constanza.vega@plantademo.cl", "cliente", "1234", "CLIENTE",
                null, "Planta Demo 1"));
    }
}
