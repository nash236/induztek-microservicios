package cl.induztek.protocolos.controller;

import cl.induztek.protocolos.dto.AsignacionRequest;
import cl.induztek.protocolos.dto.RechazoSolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudResponse;
import cl.induztek.protocolos.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService servicio;

    public SolicitudController(SolicitudService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(solicitud));
    }

    @GetMapping
    public List<SolicitudResponse> listar(
            @RequestParam(value = "instalacion", required = false) String instalacion,
            @RequestParam(value = "tecnico", required = false) String tecnico) {
        return servicio.listar(instalacion, tecnico);
    }

    @PatchMapping("/{id}/asignar")
    public SolicitudResponse asignar(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionRequest solicitud,
            @RequestHeader(value = "X-Rol", required = false) String rol) {
        return servicio.asignar(id, solicitud, rol);
    }

    @PatchMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazoSolicitudRequest solicitud,
            @RequestHeader(value = "X-Rol", required = false) String rol) {
        return servicio.rechazar(id, solicitud, rol);
    }

    @PatchMapping("/{id}/iniciar")
    public SolicitudResponse iniciar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol", required = false) String rol,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {
        return servicio.iniciar(id, rol, usuario);
    }

    @PatchMapping("/{id}/finalizar")
    public SolicitudResponse finalizar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol", required = false) String rol,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {
        return servicio.finalizar(id, rol, usuario);
    }
}
