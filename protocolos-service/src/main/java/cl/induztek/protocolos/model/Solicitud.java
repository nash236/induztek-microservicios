package cl.induztek.protocolos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String clienteUsuario;

    @Column(nullable = false, length = 120)
    private String clienteNombre;

    @Column(nullable = false, length = 120)
    private String instalacion;

    @Column(length = 20)
    private String equipoCodigo;

    @Column(nullable = false, length = 60)
    private String tipoServicio;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(nullable = false)
    private long fechaCreacion;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 50)
    private String tecnicoUsuario;

    @Column(length = 120)
    private String tecnicoNombre;

    @Column(length = 120)
    private String asignadoPorNombre;

    @Column(length = 500)
    private String indicaciones;

    @Column(length = 500)
    private String motivoRechazo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteUsuario() { return clienteUsuario; }
    public void setClienteUsuario(String clienteUsuario) { this.clienteUsuario = clienteUsuario; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getInstalacion() { return instalacion; }
    public void setInstalacion(String instalacion) { this.instalacion = instalacion; }

    public String getEquipoCodigo() { return equipoCodigo; }
    public void setEquipoCodigo(String equipoCodigo) { this.equipoCodigo = equipoCodigo; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTecnicoUsuario() { return tecnicoUsuario; }
    public void setTecnicoUsuario(String tecnicoUsuario) { this.tecnicoUsuario = tecnicoUsuario; }

    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }

    public String getAsignadoPorNombre() { return asignadoPorNombre; }
    public void setAsignadoPorNombre(String asignadoPorNombre) { this.asignadoPorNombre = asignadoPorNombre; }

    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
