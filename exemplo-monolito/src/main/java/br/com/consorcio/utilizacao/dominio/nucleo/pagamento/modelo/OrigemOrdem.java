package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;

/** Uma parte do valor da ordem: de uma cota do processo ou de um recurso. */
@Entity
@Table(name = "pag_ordem_origem")
public class OrigemOrdem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ordem_id")
    private OrdemPagamento ordem;

    @Column(name = "processo_cota_id")
    private Long processoCotaId;

    @Column(name = "recurso_id")
    private Long recursoId;

    private BigDecimal valor;

    protected OrigemOrdem() {
    }

    OrigemOrdem(OrdemPagamento ordem, Long processoCotaId, Long recursoId, BigDecimal valor) {
        RegraNegocioException.exigir((processoCotaId == null) != (recursoId == null), "ORIGEM_INVALIDA",
                "A origem é uma cota ou um recurso, nunca os dois");
        this.ordem = ordem;
        this.processoCotaId = processoCotaId;
        this.recursoId = recursoId;
        this.valor = valor;
    }

    public Long id() { return id; }

    public Long processoCotaId() { return processoCotaId; }

    public BigDecimal valor() { return valor; }
}
