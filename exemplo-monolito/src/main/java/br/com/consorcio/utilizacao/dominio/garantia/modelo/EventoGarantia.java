package br.com.consorcio.utilizacao.dominio.garantia.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Append-only. */
@Entity
@Table(name = "gar_evento")
public class EventoGarantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "garantia_id")
    private Long garantiaId;

    private String tipo;

    private String ator;

    private LocalDateTime em;

    protected EventoGarantia() {
    }

    public EventoGarantia(Long garantiaId, String tipo, String ator) {
        this.garantiaId = garantiaId;
        this.tipo = tipo;
        this.ator = ator;
        this.em = LocalDateTime.now();
    }
}
