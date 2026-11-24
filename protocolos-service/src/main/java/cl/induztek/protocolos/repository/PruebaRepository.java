package cl.induztek.protocolos.repository;

import cl.induztek.protocolos.model.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PruebaRepository extends JpaRepository<Prueba, Long> {

    List<Prueba> findByEquipoIdOrderByFechaHoraDesc(Long equipoId);

    List<Prueba> findByEstadoOrderByFechaHoraDesc(String estado);

    List<Prueba> findByEquipoInstalacionOrderByFechaHoraDesc(String instalacion);

    @Query("select p.resultado, count(p) from Prueba p group by p.resultado")
    List<Object[]> contarPorResultado();
}
