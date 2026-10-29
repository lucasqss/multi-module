package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** O que o crédito paga: bem ou serviço. Também JOINED; cada plug-in traz a sua subclasse. */
@Entity
@Table(name = "prc_objeto")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_objeto")
public abstract class Objeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    private String descricao;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "valor_avaliado")
    private BigDecimal valorAvaliado;

    @OneToMany(mappedBy = "objeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContatoAcesso> contatosAcesso = new ArrayList<>();

    protected Objeto() {
    }

    protected Objeto(String descricao, BigDecimal valorNegociado) {
        RegraNegocioException.exigir(valorNegociado != null && valorNegociado.signum() > 0, "VALOR_INVALIDO",
                "Valor negociado deve ser positivo");
        this.descricao = descricao;
        this.valorNegociado = valorNegociado;
    }

    void vincular(Processo processo) {
        this.processo = processo;
    }

    public void informarAvaliacao(BigDecimal valor) {
        RegraNegocioException.exigir(valor != null && valor.signum() > 0, "VALOR_INVALIDO", "Valor avaliado deve ser positivo");
        this.valorAvaliado = valor;
    }

    public void adicionarContatoAcesso(String nome, String email, String telefone) {
        contatosAcesso.add(new ContatoAcesso(this, nome, email, telefone));
    }

    /** Base da carta: o menor entre negociado e avaliado. O saldo da cota limita depois. */
    public BigDecimal valorBase() {
        return valorAvaliado == null ? valorNegociado : valorNegociado.min(valorAvaliado);
    }

    public Long id() { return id; }

    public String descricao() { return descricao; }

    public BigDecimal valorNegociado() { return valorNegociado; }

    public BigDecimal valorAvaliado() { return valorAvaliado; }
}
