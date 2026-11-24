package cl.induztek.protocolos.config;

import cl.induztek.protocolos.dto.EquipoRequest;
import cl.induztek.protocolos.repository.EquipoRepository;
import cl.induztek.protocolos.service.ProtocoloService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final EquipoRepository equipoRepository;
    private final ProtocoloService servicio;

    public DatosIniciales(EquipoRepository equipoRepository, ProtocoloService servicio) {
        this.equipoRepository = equipoRepository;
        this.servicio = servicio;
    }

    @Override
    public void run(String... args) {
        if (equipoRepository.count() > 0) {
            return;
        }
        servicio.crearEquipo(new EquipoRequest("TR-0012", "Transformador", "Subestación Norte (ficticia)"), "INGENIERO");
        servicio.crearEquipo(new EquipoRequest("RP-0045", "Relé de protección", "Planta Demo 1"), "INGENIERO");
        servicio.crearEquipo(new EquipoRequest("CE-0007", "Celda", "Planta Demo 2"), "INGENIERO");
    }
}
