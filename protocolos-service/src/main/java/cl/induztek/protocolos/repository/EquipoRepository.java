package cl.induztek.protocolos.repository;

import cl.induztek.protocolos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByCodigo(String codigo);

    List<Equipo> findAllByOrderByCodigoAsc();

    List<Equipo> findByInstalacionOrderByCodigoAsc(String instalacion);
}
