package cl.induztek.auth.controller;

import cl.induztek.auth.dto.LoginRequest;
import cl.induztek.auth.dto.RegistroRequest;
import cl.induztek.auth.dto.UsuarioResponse;
import cl.induztek.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/auth/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }

    @PostMapping("/auth/login")
    public UsuarioResponse iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return servicio.iniciarSesion(solicitud);
    }

    @GetMapping("/usuarios/{usuario}")
    public UsuarioResponse buscar(@PathVariable String usuario) {
        return servicio.buscar(usuario);
    }
}
