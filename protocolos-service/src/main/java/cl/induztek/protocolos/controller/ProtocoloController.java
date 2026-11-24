package cl.induztek.protocolos.controller;

import cl.induztek.protocolos.dto.AprobacionRequest;
import cl.induztek.protocolos.dto.DatoResumen;
import cl.induztek.protocolos.dto.EquipoRequest;
import cl.induztek.protocolos.dto.EquipoResponse;
import cl.induztek.protocolos.dto.PruebaRequest;
import cl.induztek.protocolos.dto.PruebaResponse;
import cl.induztek.protocolos.dto.RechazoRequest;
import cl.induztek.protocolos.service.ProtocoloService;
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
@RequestMapping("/api")
public class ProtocoloController {

    private final ProtocoloService servicio;

    public ProtocoloController(ProtocoloService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/equipos")
    public List<EquipoResponse> listarEquipos(
            @RequestParam(value = "instalacion", required = false) String instalacion) {
        return servicio.listarEquipos(instalacion);
    }

    @PostMapping("/equipos")
    public ResponseEntity<EquipoResponse> crearEquipo(
            @Valid @RequestBody EquipoRequest solicitud,
            @RequestHeader(value = "X-Rol", required = false) String rol) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crearEquipo(solicitud, rol));
    }

    @GetMapping("/equipos/{id}/pruebas")
    public List<PruebaResponse> listarPruebas(@PathVariable Long id) {
        return servicio.listarPruebas(id);
    }

    @PostMapping("/equipos/{id}/pruebas")
    public ResponseEntity<PruebaResponse> registrarPrueba(
            @PathVariable Long id,
            @Valid @RequestBody PruebaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarPrueba(id, solicitud));
    }

    @GetMapping("/clientes/{instalacion}/trabajos")
    public List<PruebaResponse> trabajosDelCliente(@PathVariable String instalacion) {
        return servicio.listarTrabajosDeInstalacion(instalacion);
    }

    @GetMapping("/pruebas/pendientes")
    public List<PruebaResponse> listarPendientes() {
        return servicio.listarPendientes();
    }

    @PatchMapping("/pruebas/{id}/aprobar")
    public PruebaResponse aprobar(
            @PathVariable Long id,
            @Valid @RequestBody AprobacionRequest solicitud,
            @RequestHeader(value = "X-Rol", required = false) String rol) {
        return servicio.aprobar(id, solicitud, rol);
    }

    @PatchMapping("/pruebas/{id}/rechazar")
    public PruebaResponse rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazoRequest solicitud,
            @RequestHeader(value = "X-Rol", required = false) String rol) {
        return servicio.rechazar(id, solicitud, rol);
    }

    @GetMapping("/resumen/resultados")
    public List<DatoResumen> resumenPorResultado() {
        return servicio.resumenPorResultado();
    }
}
