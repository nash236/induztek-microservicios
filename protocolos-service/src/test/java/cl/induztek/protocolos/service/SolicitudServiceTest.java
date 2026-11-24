package cl.induztek.protocolos.service;

import cl.induztek.protocolos.dto.AsignacionRequest;
import cl.induztek.protocolos.dto.RechazoSolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudRequest;
import cl.induztek.protocolos.dto.SolicitudResponse;
import cl.induztek.protocolos.exception.AccesoDenegadoException;
import cl.induztek.protocolos.exception.EstadoInvalidoException;
import cl.induztek.protocolos.exception.RecursoNoEncontradoException;
import cl.induztek.protocolos.model.Solicitud;
import cl.induztek.protocolos.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository repositorio;

    private SolicitudService servicio;

    @BeforeEach
    void preparar() {
        servicio = new SolicitudService(repositorio);
    }

    private Solicitud solicitud(String estado) {
        Solicitud s = new Solicitud();
        s.setId(1L);
        s.setClienteUsuario("cliente");
        s.setClienteNombre("Constanza Vega");
        s.setInstalacion("Planta Demo 1");
        s.setEquipoCodigo("RP-0045");
        s.setTipoServicio("Pruebas eléctricas");
        s.setDescripcion("Revisar el relé después de un corte de energía");
        s.setPrioridad("Urgente");
        s.setFechaCreacion(1000L);
        s.setEstado(estado);
        if (SolicitudService.ASIGNADA.equals(estado) || SolicitudService.EN_PROCESO.equals(estado)) {
            s.setTecnicoUsuario("tecnico");
            s.setTecnicoNombre("Carlos Soto");
        }
        return s;
    }

    private AsignacionRequest asignacion() {
        return new AsignacionRequest("tecnico", "Carlos Soto", "Marcela Rojas", "Revisar ajustes");
    }

    @Test
    void crear_dejaElPedidoSolicitado() {
        when(repositorio.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setId(3L);
            return s;
        });

        SolicitudResponse respuesta = servicio.crear(new SolicitudRequest(
                "cliente", "Constanza Vega", "Planta Demo 1", "RP-0045", "Reparación",
                "El equipo hace un ruido extraño al operar", "urgente", null));

        assertEquals(SolicitudService.SOLICITADA, respuesta.estado());
        assertEquals("Urgente", respuesta.prioridad());
        assertEquals(3L, respuesta.id());
    }

    @Test
    void listar_porInstalacion_usaElFiltroDelCliente() {
        when(repositorio.findByInstalacionOrderByFechaCreacionDesc("Planta Demo 1"))
                .thenReturn(List.of(solicitud(SolicitudService.SOLICITADA)));

        assertEquals(1, servicio.listar(" Planta Demo 1 ", null).size());
    }

    @Test
    void listar_porTecnico_devuelveSoloSusTareas() {
        when(repositorio.findByTecnicoUsuarioOrderByFechaCreacionDesc("tecnico"))
                .thenReturn(List.of(solicitud(SolicitudService.ASIGNADA)));

        assertEquals(1, servicio.listar(null, "tecnico").size());
    }

    @Test
    void listar_sinFiltros_devuelveTodas() {
        when(repositorio.findAllByOrderByFechaCreacionDesc())
                .thenReturn(List.of(solicitud(SolicitudService.SOLICITADA), solicitud(SolicitudService.ASIGNADA)));

        assertEquals(2, servicio.listar(null, null).size());
    }

    @Test
    void asignar_comoTecnico_lanzaAccesoDenegado() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.asignar(1L, asignacion(), "TECNICO"));
        verify(repositorio, never()).save(any(Solicitud.class));
    }

    @Test
    void asignar_comoIngeniero_delegaEnUnTecnico() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.SOLICITADA)));
        when(repositorio.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse respuesta = servicio.asignar(1L, asignacion(), "INGENIERO");

        assertEquals(SolicitudService.ASIGNADA, respuesta.estado());
        assertEquals("tecnico", respuesta.tecnicoUsuario());
        assertEquals("Marcela Rojas", respuesta.asignadoPorNombre());
    }

    @Test
    void asignar_pedidoYaAsignado_lanzaEstadoInvalido() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.ASIGNADA)));

        assertThrows(EstadoInvalidoException.class, () -> servicio.asignar(1L, asignacion(), "INGENIERO"));
    }

    @Test
    void asignar_pedidoInexistente_lanzaNoEncontrado() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.asignar(9L, asignacion(), "INGENIERO"));
    }

    @Test
    void rechazar_comoIngeniero_guardaElMotivo() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.SOLICITADA)));
        when(repositorio.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse respuesta = servicio.rechazar(
                1L, new RechazoSolicitudRequest("Fuera de nuestra cobertura"), "INGENIERO");

        assertEquals(SolicitudService.RECHAZADA, respuesta.estado());
        assertEquals("Fuera de nuestra cobertura", respuesta.motivoRechazo());
    }

    @Test
    void iniciar_deOtroTecnico_lanzaAccesoDenegado() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.ASIGNADA)));

        assertThrows(AccesoDenegadoException.class, () -> servicio.iniciar(1L, "TECNICO", "otro"));
    }

    @Test
    void iniciar_elTecnicoAsignado_pasaAEnProceso() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.ASIGNADA)));
        when(repositorio.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse respuesta = servicio.iniciar(1L, "TECNICO", "tecnico");

        assertEquals(SolicitudService.EN_PROCESO, respuesta.estado());
    }

    @Test
    void finalizar_sinHaberIniciado_lanzaEstadoInvalido() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.ASIGNADA)));

        assertThrows(EstadoInvalidoException.class, () -> servicio.finalizar(1L, "TECNICO", "tecnico"));
    }

    @Test
    void finalizar_cuandoEstaEnProceso_completaElServicio() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(solicitud(SolicitudService.EN_PROCESO)));
        when(repositorio.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse respuesta = servicio.finalizar(1L, "TECNICO", "tecnico");

        assertEquals(SolicitudService.COMPLETADA, respuesta.estado());
    }
}
