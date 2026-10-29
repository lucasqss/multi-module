package br.com.consorcio.utilizacao.dominio.garantia.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gar_garantia")
public class Garantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cota_ref")
    private String cotaRef;

    /** FIANCA, AF_IMOVEL, AF_VEICULO, ... */
    private String tipo;

    /** CONTEMPLACAO, PROCESSO_UTILIZACAO, SUBSTITUICAO */
    private String origem;

    @Column(name = "processo_ref")
    private Long processoRef;

    @Column(name = "objeto_ref")
    private Long objetoRef;

    @Column(name = "instrumento_ref")
    private Long instrumentoRef;

    @Column(name = "valor_cobertura")
    private BigDecimal valorCobertura;

    @Enumerated(EnumType.STRING)
    private SituacaoGarantia situacao;

    @Column(name = "chave_idempotencia")
    private String chaveIdempotencia;

    @OneToMany(mappedBy = "garantia", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<RegistroGarantia> registros = new ArrayList<>();

    @OneToMany(mappedBy = "garantia", cascade = CascadeType.ALL)
    private List<Fiador> fiadores = new ArrayList<>();

    protected Garantia() {
    }

    public static Garantia fianca(String cotaRef, String cpfCnpjFiador, String nomeFiador, BigDecimal valor, String chave) {
        Garantia g = nova(cotaRef, "FIANCA", "CONTEMPLACAO", valor);
        g.situacao = SituacaoGarantia.CONSTITUIDA;
        g.chaveIdempotencia = chave;
        g.fiadores.add(new Fiador(g, cpfCnpjFiador, nomeFiador));
        return g;
    }

    public static Garantia alienacao(String cotaRef, String tipo, Long processoRef, Long objetoRef, Long instrumentoRef, BigDecimal valor) {
        Garantia g = nova(cotaRef, tipo, "PROCESSO_UTILIZACAO", valor);
        g.situacao = SituacaoGarantia.PROPOSTA;
        g.processoRef = processoRef;
        g.objetoRef = objetoRef;
        g.instrumentoRef = instrumentoRef;
        return g;
    }

    public void protocolar(String orgao, String protocolo) {
        exigir(SituacaoGarantia.PROPOSTA, SituacaoGarantia.EM_CONSTITUICAO);
        registros.add(new RegistroGarantia(this, orgao, protocolo));
        situacao = SituacaoGarantia.EM_CONSTITUICAO;
    }

    public void registrar(String numeroRegistro, LocalDate registradoEm) {
        exigir(SituacaoGarantia.EM_CONSTITUICAO);
        ultimoRegistro().registrar(numeroRegistro, registradoEm);
    }

    public void conferir(String conferente) {
        exigir(SituacaoGarantia.EM_CONSTITUICAO);
        ultimoRegistro().conferir(conferente);
        situacao = SituacaoGarantia.CONSTITUIDA;
    }

    /** Só o que ainda não produziu efeito perante terceiros pode ser cancelado sem tratamento jurídico. */
    public boolean cancelarSeNaoConstituida() {
        if (situacao == SituacaoGarantia.PROPOSTA || situacao == SituacaoGarantia.EM_CONSTITUICAO) {
            situacao = SituacaoGarantia.CANCELADA;
            return true;
        }
        return false;
    }

    public boolean constituida() {
        return situacao == SituacaoGarantia.CONSTITUIDA;
    }

    private RegistroGarantia ultimoRegistro() {
        RegraNegocioException.exigir(!registros.isEmpty(), "SEM_PROTOCOLO", "Garantia " + id + " ainda não foi protocolada");
        return registros.get(registros.size() - 1);
    }

    private void exigir(SituacaoGarantia... permitidas) {
        for (SituacaoGarantia s : permitidas) {
            if (situacao == s) {
                return;
            }
        }
        throw new RegraNegocioException("GARANTIA_SITUACAO_INVALIDA", "Garantia " + id + " está " + situacao);
    }

    private static Garantia nova(String cotaRef, String tipo, String origem, BigDecimal valor) {
        RegraNegocioException.exigir(valor != null && valor.signum() > 0, "COBERTURA_INVALIDA", "Valor de cobertura deve ser positivo");
        Garantia g = new Garantia();
        g.cotaRef = cotaRef;
        g.tipo = tipo;
        g.origem = origem;
        g.valorCobertura = valor;
        return g;
    }

    public Long id() { return id; }

    public String cotaRef() { return cotaRef; }

    public String tipo() { return tipo; }

    public SituacaoGarantia situacao() { return situacao; }

    public BigDecimal valorCobertura() { return valorCobertura; }
}
