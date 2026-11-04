package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.specification.pagamento.RegrasPagamento;
import com.example.pagamento.domain.specification.pagamento.RetornoPagamento;
import com.example.pagamento.domain.specification.pagamento.SubmissaoPagamento;
import com.example.pagamento.domain.state.EstadoPagamento;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.PagamentoId;
import com.example.pagamento.domain.valueobject.RetornoBanco;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root: pagamento de um ou mais boletos. O boleto em si é de um sistema externo (o emissor);
 * aqui ficam só cópias dos dados ({@link BoletosDoPagamento}).
 *
 * <p>Domínio RICO: sem setters; o estado só muda por métodos de negócio, sempre nesta ordem:
 * 1º o ESTADO diz se a ação é permitida (409); 2º as REGRAS validam (422) ou, para fatos externos,
 * classificam; 3º só então o estado muda. As regras que variam por {@link Modalidade} ficam na
 * cadeia de {@link RegrasPagamento}, não em {@code if}s aqui.
 */
public class Pagamento {

    private final PagamentoId id;
    private final Modalidade modalidade;
    private final Instant criadoEm;
    private BoletosDoPagamento boletos;
    private ContaDebito contaDebito;
    private LocalDate dataPagamento;
    private EstadoPagamento estado;
    private int envios;
    private String idTransacao;
    private String autenticacao;
    private String motivoRecusa;
    private MotivoAnalise motivoAnalise;
    private Motivo motivoCancelamento;

    private Pagamento(PagamentoId id, Modalidade modalidade, Instant criadoEm, BoletosDoPagamento boletos,
                      ContaDebito contaDebito, LocalDate dataPagamento, EstadoPagamento estado, int envios, String idTransacao,
                      String autenticacao, String motivoRecusa, MotivoAnalise motivoAnalise, Motivo motivoCancelamento) {
        this.id = Objects.requireNonNull(id);
        this.modalidade = Objects.requireNonNull(modalidade);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.boletos = Objects.requireNonNull(boletos);
        this.contaDebito = contaDebito;
        this.dataPagamento = dataPagamento;
        this.estado = Objects.requireNonNull(estado);
        this.envios = envios;
        this.idTransacao = idTransacao;
        this.autenticacao = autenticacao;
        this.motivoRecusa = motivoRecusa;
        this.motivoAnalise = motivoAnalise;
        this.motivoCancelamento = motivoCancelamento;
    }

    // =============================== criação ===============================

    /** Cria o rascunho (EM_EDICAO) só com a modalidade. Completude é exigida em {@link #submeter}. */
    public static Pagamento criar(Modalidade modalidade, Instant agora) {
        return new Pagamento(PagamentoId.novo(), modalidade, agora, BoletosDoPagamento.NENHUM, null, null,
                EstadoPagamento.inicial(), 0, null, null, null, null, null);
    }

    /** Reconstrói a partir da persistência, sem reaplicar regras (o dado já foi validado). */
    public static Pagamento reconstituir(PagamentoId id, Modalidade modalidade, Instant criadoEm,
                                         BoletosDoPagamento boletos, ContaDebito contaDebito, LocalDate dataPagamento,
                                         StatusPagamento status, int envios, String idTransacao, String autenticacao,
                                         String motivoRecusa, MotivoAnalise motivoAnalise, Motivo motivoCancelamento) {
        return new Pagamento(id, modalidade, criadoEm, boletos, contaDebito, dataPagamento, EstadoPagamento.de(status),
                envios, idTransacao, autenticacao, motivoRecusa, motivoAnalise, motivoCancelamento);
    }

    // =============================== edição por partes (só em EM_EDICAO) ===============================

    /** Seção "boletos": substitui inteira. Só as regras da seção; o pagamento pode continuar incompleto. */
    public void salvarBoletos(BoletosDoPagamento novos) {
        estado.validarEdicao();
        RegrasPagamento.BOLETOS.validar(novos);
        this.boletos = novos;
    }

    /** Seção "dados gerais". Regras que dependem de "hoje" valem na submissão. */
    public void alterarDadosGerais(ContaDebito novaConta, LocalDate novaData) {
        estado.validarEdicao();
        this.contaDebito = novaConta;
        this.dataPagamento = novaData;
    }

    public void validarExclusao() {
        estado.validarExclusao();
    }

    // =============================== transições (comandos do usuário) ===============================

    /** Finaliza a edição: completude + regras da modalidade (Chain of Responsibility). EM_EDICAO -> AGENDADO. */
    public void submeter(LocalDate hoje) {
        var proximo = estado.submeter();
        RegrasPagamento.SUBMISSAO.validar(new SubmissaoPagamento(modalidade, boletos, contaDebito, dataPagamento, hoje));
        estado = proximo;
    }

    /** AGENDADO -> EM_PROCESSAMENTO, com uma nova transação (o id é nosso; vai ao banco e volta no retorno). */
    public void enviarAoBanco() {
        estado = estado.enviarAoBanco();
        novaTransacao();
    }

    /** RECUSADO -> EM_PROCESSAMENTO, com outra transação. */
    public void reenviarAoBanco() {
        estado = estado.reenviarAoBanco();
        novaTransacao();
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== fato externo (retorno do banco) ===============================

    /**
     * O banco informa o resultado. É um FATO: a conformidade CLASSIFICA (não lança).
     * Idempotente: retorno de uma transação que não é a atual (repetido ou antigo) é ignorado.
     */
    public void registrarRetornoBanco(RetornoBanco retorno) {
        if (!retorno.idTransacao().equals(idTransacao) || autenticacao != null || motivoRecusa != null) {
            return; // aviso repetido ou de outra transação: já processado
        }
        var divergencia = RegrasPagamento.CONFORMIDADE_RETORNO
                .primeiraViolacao(new RetornoPagamento(getValorTotal(), retorno));

        estado = estado.receberRetornoBanco(retorno.liquidado(), divergencia.isEmpty());
        autenticacao = retorno.codigoAutenticacao();
        motivoRecusa = retorno.motivoRecusa();
        motivoAnalise = divergencia.map(MotivoAnalise::de).orElse(null);
    }

    private void novaTransacao() {
        envios++;
        idTransacao = id + "-" + envios;
        autenticacao = null;
        motivoRecusa = null;
    }

    // =============================== leitura ===============================

    public PagamentoId getId() { return id; }
    public Modalidade getModalidade() { return modalidade; }
    public Instant getCriadoEm() { return criadoEm; }
    public BoletosDoPagamento getBoletos() { return boletos; }
    /** Valor do pagamento = soma dos boletos. */
    public Dinheiro getValorTotal() { return boletos.total(); }
    public Optional<ContaDebito> getContaDebito() { return Optional.ofNullable(contaDebito); }
    public Optional<LocalDate> getDataPagamento() { return Optional.ofNullable(dataPagamento); }
    public StatusPagamento getStatus() { return estado.status(); }
    public int getEnvios() { return envios; }
    public Optional<String> getIdTransacao() { return Optional.ofNullable(idTransacao); }
    public Optional<String> getAutenticacao() { return Optional.ofNullable(autenticacao); }
    public Optional<String> getMotivoRecusa() { return Optional.ofNullable(motivoRecusa); }
    public Optional<MotivoAnalise> getMotivoAnalise() { return Optional.ofNullable(motivoAnalise); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
