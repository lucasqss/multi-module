package br.com.consorcio.utilizacao.dominio.nucleo.partes.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.compartilhado.SimNaoConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "pes_conta_recebimento")
public class ContaRecebimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participacao_id")
    private Participacao participacao;

    private String banco;

    private String agencia;

    private String conta;

    @Column(name = "chave_pix")
    private String chavePix;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "titularidade_verificada")
    private boolean titularidadeVerificada;

    protected ContaRecebimento() {
    }

    public ContaRecebimento(Participacao participacao, String banco, String agencia, String conta, String chavePix,
                            boolean titularidadeVerificada) {
        RegraNegocioException.exigir(chavePix != null || (banco != null && conta != null), "CONTA_INCOMPLETA",
                "Informe banco e conta, ou chave Pix");
        this.participacao = participacao;
        this.banco = banco;
        this.agencia = agencia;
        this.conta = conta;
        this.chavePix = chavePix;
        this.titularidadeVerificada = titularidadeVerificada;
    }

    /** Lança se a conta não puder receber uma ordem para este beneficiário. */
    public void exigirAptaPara(Long participacaoId) {
        RegraNegocioException.exigir(participacao.id().equals(participacaoId), "CONTA_DE_OUTRO_BENEFICIARIO",
                "A conta não pertence ao beneficiário da ordem");
        RegraNegocioException.exigir(titularidadeVerificada, "CONTA_NAO_VERIFICADA", "Titularidade da conta não verificada");
    }

    public Long id() { return id; }

    public Participacao participacao() { return participacao; }

    public String banco() { return banco; }

    public String agencia() { return agencia; }

    public String conta() { return conta; }

    public String chavePix() { return chavePix; }
}
