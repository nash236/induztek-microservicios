package cl.induztek.protocolos.service;

import cl.induztek.protocolos.dto.AprobacionRequest;
import cl.induztek.protocolos.dto.DatoResumen;
import cl.induztek.protocolos.dto.EquipoRequest;
import cl.induztek.protocolos.dto.EquipoResponse;
import cl.induztek.protocolos.dto.MedicionDto;
import cl.induztek.protocolos.dto.PruebaRequest;
import cl.induztek.protocolos.dto.PruebaResponse;
import cl.induztek.protocolos.dto.RechazoRequest;
import cl.induztek.protocolos.exception.AccesoDenegadoException;
import cl.induztek.protocolos.exception.EquipoDuplicadoException;
import cl.induztek.protocolos.exception.EstadoInvalidoException;
import cl.induztek.protocolos.exception.RecursoNoEncontradoException;
import cl.induztek.protocolos.model.Equipo;
import cl.induztek.protocolos.model.Prueba;
import cl.induztek.protocolos.repository.EquipoRepository;
import cl.induztek.protocolos.repository.PruebaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocoloServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private PruebaRepository pruebaRepository;

    private ProtocoloService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ProtocoloService(equipoRepository, pruebaRepository);
    }

    private Equipo equipo() {
        Equipo e = new Equipo();
        e.setId(1L);
        e.setCodigo("TR-0012");
        e.setTipo("Transformador");
        e.setInstalacion("Planta Demo");
        return e;
    }

    private Prueba pruebaPendiente() {
        Prueba p = new Prueba();
        p.setId(10L);
        p.setEquipo(equipo());
        p.setFechaHora(1000L);
        p.setResultado(ProtocoloService.RESULTADO_PENDIENTE);
        p.setEstado(ProtocoloService.REGISTRADO_EN_TERRENO);
        p.setObservaciones("");
        p.setRegistradoPor("tecnico");
        p.setRegistradoPorNombre("Carlos Soto");
        return p;
    }

    @Test
    void crearEquipo_comoTecnico_guardaElEquipo() {
        when(equipoRepository.existsByCodigo("TR-0098")).thenReturn(false);
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> {
            Equipo e = inv.getArgument(0);
            e.setId(8L);
            return e;
        });

        EquipoResponse respuesta = servicio.crearEquipo(
                new EquipoRequest("TR-0098", "Transformador", "Planta 1"), "TECNICO");

        assertEquals(8L, respuesta.id());
    }

    @Test
    void crearEquipo_comoCliente_lanzaAccesoDenegado() {
        EquipoRequest solicitud = new EquipoRequest("TR-0099", "Transformador", "Planta 1");

        assertThrows(AccesoDenegadoException.class, () -> servicio.crearEquipo(solicitud, "CLIENTE"));
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_sinRol_lanzaAccesoDenegado() {
        EquipoRequest solicitud = new EquipoRequest("TR-0099", "Transformador", "Planta 1");

        assertThrows(AccesoDenegadoException.class, () -> servicio.crearEquipo(solicitud, null));
    }

    @Test
    void crearEquipo_conCodigoRepetido_lanzaConflicto() {
        when(equipoRepository.existsByCodigo("TR-0012")).thenReturn(true);
        EquipoRequest solicitud = new EquipoRequest("TR-0012", "Transformador", "Planta 1");

        assertThrows(EquipoDuplicadoException.class, () -> servicio.crearEquipo(solicitud, "INGENIERO"));
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    void crearEquipo_comoIngeniero_guardaYDevuelveEquipo() {
        when(equipoRepository.existsByCodigo("TR-0099")).thenReturn(false);
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> {
            Equipo e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });

        EquipoResponse respuesta = servicio.crearEquipo(
                new EquipoRequest("tr-0099", "Transformador", "Planta 1"), "ingeniero");

        assertEquals(5L, respuesta.id());
        assertEquals("TR-0099", respuesta.codigo());
    }

    @Test
    void registrarPrueba_quedaPendienteSinResultado() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo()));
        when(pruebaRepository.save(any(Prueba.class))).thenAnswer(inv -> {
            Prueba p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PruebaRequest solicitud = new PruebaRequest(
                "Humedad leve", "tecnico", "Carlos Soto",
                List.of(new MedicionDto("Resistencia", 1150.0, "MΩ"), new MedicionDto("Relación", 1.003, "")),
                null);

        PruebaResponse respuesta = servicio.registrarPrueba(1L, solicitud);

        assertEquals(ProtocoloService.REGISTRADO_EN_TERRENO, respuesta.estado());
        assertEquals(ProtocoloService.RESULTADO_PENDIENTE, respuesta.resultado());
        assertEquals("Carlos Soto", respuesta.registradoPorNombre());
        assertNull(respuesta.revisadoPor());
        assertEquals(2, respuesta.mediciones().size());
        assertEquals("TR-0012", respuesta.codigoEquipo());
    }

    @Test
    void registrarPrueba_conEquipoInexistente_lanzaNoEncontrado() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());
        PruebaRequest solicitud = new PruebaRequest(
                null, "tecnico", "Carlos Soto", List.of(new MedicionDto("Resistencia", 1.0, "MΩ")), null);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrarPrueba(99L, solicitud));
    }

    @Test
    void aprobar_comoTecnico_lanzaAccesoDenegado() {
        AprobacionRequest solicitud = new AprobacionRequest("tecnico", "Carlos Soto");

        assertThrows(AccesoDenegadoException.class, () -> servicio.aprobar(10L, solicitud, "TECNICO"));
        verify(pruebaRepository, never()).save(any(Prueba.class));
    }

    @Test
    void aprobar_comoIngeniero_formalizaLaPrueba() {
        when(pruebaRepository.findById(10L)).thenReturn(Optional.of(pruebaPendiente()));
        when(pruebaRepository.save(any(Prueba.class))).thenAnswer(inv -> inv.getArgument(0));

        PruebaResponse respuesta = servicio.aprobar(
                10L, new AprobacionRequest("ingeniero", "Marcela Rojas"), "INGENIERO");

        assertEquals(ProtocoloService.FORMALIZADO, respuesta.estado());
        assertEquals(ProtocoloService.RESULTADO_APROBADO, respuesta.resultado());
        assertEquals("Marcela Rojas", respuesta.revisadoPorNombre());
    }

    @Test
    void aprobar_pruebaYaRevisada_lanzaEstadoInvalido() {
        Prueba prueba = pruebaPendiente();
        prueba.setEstado(ProtocoloService.FORMALIZADO);
        when(pruebaRepository.findById(10L)).thenReturn(Optional.of(prueba));

        assertThrows(EstadoInvalidoException.class,
                () -> servicio.aprobar(10L, new AprobacionRequest("ingeniero", "Marcela Rojas"), "INGENIERO"));
    }

    @Test
    void aprobar_pruebaInexistente_lanzaNoEncontrado() {
        when(pruebaRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.aprobar(77L, new AprobacionRequest("ingeniero", "Marcela Rojas"), "INGENIERO"));
    }

    @Test
    void rechazar_comoTecnico_lanzaAccesoDenegado() {
        RechazoRequest solicitud = new RechazoRequest("tecnico", "Carlos Soto", "Mediciones incompletas");

        assertThrows(AccesoDenegadoException.class, () -> servicio.rechazar(10L, solicitud, "TECNICO"));
        verify(pruebaRepository, never()).save(any(Prueba.class));
    }

    @Test
    void rechazar_comoIngeniero_guardaElMotivo() {
        when(pruebaRepository.findById(10L)).thenReturn(Optional.of(pruebaPendiente()));
        when(pruebaRepository.save(any(Prueba.class))).thenAnswer(inv -> inv.getArgument(0));

        PruebaResponse respuesta = servicio.rechazar(
                10L, new RechazoRequest("ingeniero", "Marcela Rojas", "Falta medir la relación"), "INGENIERO");

        assertEquals(ProtocoloService.RECHAZADO, respuesta.estado());
        assertEquals(ProtocoloService.RESULTADO_RECHAZADO, respuesta.resultado());
        assertEquals("Falta medir la relación", respuesta.motivoRechazo());
        assertEquals("Marcela Rojas", respuesta.revisadoPorNombre());
    }

    @Test
    void rechazar_pruebaYaRevisada_lanzaEstadoInvalido() {
        Prueba prueba = pruebaPendiente();
        prueba.setEstado(ProtocoloService.RECHAZADO);
        when(pruebaRepository.findById(10L)).thenReturn(Optional.of(prueba));

        assertThrows(EstadoInvalidoException.class,
                () -> servicio.rechazar(10L,
                        new RechazoRequest("ingeniero", "Marcela Rojas", "Otro motivo"), "INGENIERO"));
    }

    @Test
    void listarEquipos_conInstalacion_filtraPorInstalacion() {
        when(equipoRepository.findByInstalacionOrderByCodigoAsc("Planta Demo"))
                .thenReturn(List.of(equipo()));

        List<EquipoResponse> equipos = servicio.listarEquipos(" Planta Demo ");

        assertEquals(1, equipos.size());
        assertEquals("TR-0012", equipos.get(0).codigo());
    }

    @Test
    void listarTrabajosDeInstalacion_devuelveLasPruebasDeEsaInstalacion() {
        when(pruebaRepository.findByEquipoInstalacionOrderByFechaHoraDesc("Planta Demo"))
                .thenReturn(List.of(pruebaPendiente()));

        List<PruebaResponse> trabajos = servicio.listarTrabajosDeInstalacion(" Planta Demo ");

        assertEquals(1, trabajos.size());
        assertEquals("TR-0012", trabajos.get(0).codigoEquipo());
    }

    @Test
    void resumenPorResultado_conviertePares() {
        List<Object[]> filas = new ArrayList<>();
        filas.add(new Object[]{"Aprobado", 3L});
        filas.add(new Object[]{"Pendiente", 1L});
        when(pruebaRepository.contarPorResultado()).thenReturn(filas);

        List<DatoResumen> resumen = servicio.resumenPorResultado();

        assertEquals(2, resumen.size());
        assertEquals("Aprobado", resumen.get(0).etiqueta());
        assertEquals(3L, resumen.get(0).valor());
    }
}
