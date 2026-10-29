package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.SituacaoEtapa;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Uma linha por fase do roteiro, criada na abertura. Só o processo a altera. */
@Entity
@Table(name = "prc_etapa")
public class Etapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @Enumerated(EnumType.STRING)
    @Column(name = "codigo_fase")
    private Fase fase;

    private int ordem;

    @Enumerated(EnumType.STRING)
    private SituacaoEtapa situacao = SituacaoEtapa.NAO_INICIADA;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @Column(name = "concluida_por")
    private String concluidaPor;

    protected Etapa() {
    }

    Etapa(Processo processo, Fase fase, int ordem) {
        this.processo = processo;
        this.fase = fase;
        this.ordem = ordem;
    }

    void concluir(String ator) {
        situacao = SituacaoEtapa.CONCLUIDA;
        concluidaEm = LocalDateTime.now();
        concluidaPor = ator;
    }

    public Long id() { return id; }

    public Fase fase() { return fase; }

    public SituacaoEtapa situacao() { return situacao; }
}
