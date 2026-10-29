package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.FinalidadeOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.SituacaoOrdem;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static br.com.consorcio.utilizacao.compartilhado.RegraNegocioException.exigir;

@Entity
@Table(name = "pag_ordem")
public class OrdemPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    @Column(name = "beneficiario_participacao_id")
    private Long beneficiarioParticipacaoId;

    @Column(name = "conta_recebimento_id")
    private Long contaRecebimentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "finalidade_ordem")
    private FinalidadeOrdem finalidade;

    private String meio;

    /** Sempre igual à soma das origens. */
    private BigDecimal valor = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private SituacaoOrdem situacao = SituacaoOrdem.PREPARADA;

    @Column(name = "preparada_por")
    private String preparadaPor;

    @Column(name = "aprovada_por")
    private String aprovadaPor;

    @Column(name = "id_externo")
    private String idExterno;

    @Version
    private long versao;

    @OneToMany(mappedBy = "ordem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrigemOrdem> origens = new ArrayList<>();

    protected OrdemPagamento() {
    }

    public static OrdemPagamento preparar(Long processoId, Long beneficiarioId, Long contaId, FinalidadeOrdem finalidade,
                                          String meio, String ator) {
        OrdemPagamento o = new OrdemPagamento();
        o.processoId = processoId;
        o.beneficiarioParticipacaoId = beneficiarioId;
        o.contaRecebimentoId = contaId;
        o.finalidade = finalidade;
        o.meio = meio;
        o.preparadaPor = ator;
        return o;
    }

    public void adicionarOrigem(Long processoCotaId, Long recursoId, BigDecimal valorOrigem) {
        exigir(situacao == SituacaoOrdem.PREPARADA, "ORDEM_SITUACAO_INVALIDA", "Origens só mudam enquanto a ordem está preparada");
        exigir(valorOrigem != null && valorOrigem.signum() > 0, "ORIGEM_INVALIDA", "Valor da origem deve ser positivo");
        origens.add(new OrigemOrdem(this, processoCotaId, recursoId, valorOrigem));
        valor = valor.add(valorOrigem);
    }

    public void aprovar(String ator) {
        exigir(situacao == SituacaoOrdem.PREPARADA, "ORDEM_SITUACAO_INVALIDA", "Ordem está " + situacao);
        exigir(!origens.isEmpty(), "ORDEM_SEM_ORIGEM", "Informe de onde vem o valor");
        exigir(!ator.equals(preparadaPor), "QUATRO_OLHOS", "Quem preparou a ordem não pode aprová-la");
        situacao = SituacaoOrdem.APROVADA;
        aprovadaPor = ator;
    }

    /** Chamado quando o sistema de pagamentos aceitou a ordem. Idempotente. */
    public void marcarEnviada(String idExterno) {
        this.idExterno = idExterno;
        if (situacao == SituacaoOrdem.APROVADA) {
            situacao = SituacaoOrdem.ENVIADA;
        }
    }

    /** O retorno pode chegar antes de a saída assíncrona marcar a ordem como enviada. */
    public void registrarResultado(String resultado) {
        exigir(situacao == SituacaoOrdem.APROVADA || situacao == SituacaoOrdem.ENVIADA, "ORDEM_SITUACAO_INVALIDA",
                "Retorno para ordem " + situacao);
        situacao = switch (resultado) {
            case "LIQUIDADO" -> SituacaoOrdem.LIQUIDADA;
            case "REJEITADO" -> SituacaoOrdem.REJEITADA;
            case "DEVOLVIDO" -> SituacaoOrdem.DEVOLVIDA;
            default -> throw new RegraNegocioException("RESULTADO_DESCONHECIDO", resultado);
        };
    }

    public Long id() { return id; }

    public Long processoId() { return processoId; }

    public Long beneficiarioParticipacaoId() { return beneficiarioParticipacaoId; }

    public Long contaRecebimentoId() { return contaRecebimentoId; }

    public FinalidadeOrdem finalidade() { return finalidade; }

    public String meio() { return meio; }

    public BigDecimal valor() { return valor; }

    public SituacaoOrdem situacao() { return situacao; }

    public String idExterno() { return idExterno; }

    public List<OrigemOrdem> origens() { return Collections.unmodifiableList(origens); }
}
