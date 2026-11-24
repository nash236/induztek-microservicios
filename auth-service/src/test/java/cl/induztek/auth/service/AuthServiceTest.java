package cl.induztek.auth.service;

import cl.induztek.auth.dto.LoginRequest;
import cl.induztek.auth.dto.RegistroRequest;
import cl.induztek.auth.dto.UsuarioResponse;
import cl.induztek.auth.exception.CodigoInvalidoException;
import cl.induztek.auth.exception.CredencialesInvalidasException;
import cl.induztek.auth.exception.UsuarioExistenteException;
import cl.induztek.auth.model.Usuario;
import cl.induztek.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repositorio;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AuthService(repositorio, encoder);
    }

    private RegistroRequest solicitud(String rol, String codigo) {
        return new RegistroRequest("Carlos", "Soto", "carlos@induztek.cl", "csoto", "secreto1", rol, codigo, null);
    }

    @Test
    void registrarTecnico_guardaUsuarioConClaveEncriptada() {
        when(repositorio.existsByUsuario("csoto")).thenReturn(false);
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = servicio.registrar(solicitud("tecnico", null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals("TECNICO", respuesta.rol());
        assertEquals("csoto", respuesta.usuario());
        assertNotEquals("secreto1", guardado.getClaveHash());
        assertTrue(encoder.matches("secreto1", guardado.getClaveHash()));
    }

    @Test
    void registrarUsuarioExistente_lanzaConflicto() {
        when(repositorio.existsByUsuario("csoto")).thenReturn(true);

        assertThrows(UsuarioExistenteException.class, () -> servicio.registrar(solicitud("TECNICO", null)));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void registrarIngenieroSinCodigo_lanzaCodigoInvalido() {
        when(repositorio.existsByUsuario("csoto")).thenReturn(false);

        assertThrows(CodigoInvalidoException.class, () -> servicio.registrar(solicitud("INGENIERO", "incorrecto")));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void registrarIngenieroConCodigoCorrecto_guardaUsuario() {
        when(repositorio.existsByUsuario("csoto")).thenReturn(false);
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = servicio.registrar(solicitud("INGENIERO", AuthService.CODIGO_INGENIERO));

        assertEquals("INGENIERO", respuesta.rol());
    }

    @Test
    void registrarClienteSinInstalacion_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(solicitud("CLIENTE", null)));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void registrarClienteConInstalacion_guardaLaEmpresa() {
        when(repositorio.existsByUsuario("cvega")).thenReturn(false);
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        RegistroRequest solicitud = new RegistroRequest(
                "Constanza", "Vega", "constanza@plantademo.cl", "cvega", "secreto1", "cliente", null, "Planta Demo 1");

        UsuarioResponse respuesta = servicio.registrar(solicitud);

        assertEquals("CLIENTE", respuesta.rol());
        assertEquals("Planta Demo 1", respuesta.empresa());
    }

    @Test
    void registrarRolInvalido_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(solicitud("GERENTE", null)));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void iniciarSesion_conCredencialesCorrectas_devuelveUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsuario("csoto");
        usuario.setClaveHash(encoder.encode("secreto1"));
        usuario.setNombre("Carlos");
        usuario.setApellido("Soto");
        usuario.setCorreo("carlos@induztek.cl");
        usuario.setRol("TECNICO");
        when(repositorio.findByUsuario("csoto")).thenReturn(Optional.of(usuario));

        UsuarioResponse respuesta = servicio.iniciarSesion(new LoginRequest("CSoto", "secreto1"));

        assertEquals("csoto", respuesta.usuario());
        assertEquals("TECNICO", respuesta.rol());
    }

    @Test
    void iniciarSesion_conClaveIncorrecta_lanzaCredencialesInvalidas() {
        Usuario usuario = new Usuario();
        usuario.setUsuario("csoto");
        usuario.setClaveHash(encoder.encode("secreto1"));
        when(repositorio.findByUsuario("csoto")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new LoginRequest("csoto", "otraClave")));
    }

    @Test
    void iniciarSesion_conUsuarioInexistente_lanzaCredencialesInvalidas() {
        when(repositorio.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new LoginRequest("fantasma", "secreto1")));
    }
}
