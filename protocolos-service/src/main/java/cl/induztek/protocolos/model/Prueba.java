package cl.induztek.protocolos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pruebas")
public class Prueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @Column(nullable = false)
    private long fechaHora;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false, length = 50)
    private String registradoPor;

    @Column(nullable = false, length = 120)
    private String registradoPorNombre;

    @Column(length = 50)
    private String revisadoPor;

    @Column(length = 120)
    private String revisadoPorNombre;

    @Column(length = 500)
    private String motivoRechazo;

    @OneToMany(mappedBy = "prueba", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medicion> mediciones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public long getFechaHora() { return fechaHora; }
    public void setFechaHora(long fechaHora) { this.fechaHora = fechaHora; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public String getRegistradoPorNombre() { return registradoPorNombre; }
    public void setRegistradoPorNombre(String registradoPorNombre) { this.registradoPorNombre = registradoPorNombre; }

    public String getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(String revisadoPor) { this.revisadoPor = revisadoPor; }

    public String getRevisadoPorNombre() { return revisadoPorNombre; }
    public void setRevisadoPorNombre(String revisadoPorNombre) { this.revisadoPorNombre = revisadoPorNombre; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public List<Medicion> getMediciones() { return mediciones; }
    public void setMediciones(List<Medicion> mediciones) { this.mediciones = mediciones; }
}
