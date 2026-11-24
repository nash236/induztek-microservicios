package cl.induztek.protocolos.repository;

import cl.induztek.protocolos.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByFechaCreacionDesc();

    List<Solicitud> findByInstalacionOrderByFechaCreacionDesc(String instalacion);

    List<Solicitud> findByTecnicoUsuarioOrderByFechaCreacionDesc(String tecnicoUsuario);
}
