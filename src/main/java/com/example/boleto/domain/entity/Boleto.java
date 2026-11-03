package com.example.boleto.domain.entity;

import com.example.boleto.domain.specification.boleto.RecebimentoPagamento;
import com.example.boleto.domain.specification.boleto.RegrasBoleto;
import com.example.boleto.domain.specification.boleto.SolicitacaoBoleto;
import com.example.boleto.domain.state.EstadoBoleto;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagador;
import com.example.boleto.domain.valueobject.Pagamento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root do boleto.
 *
 * <p>Domínio RICO:
 * <ul>
 *   <li>Sem setters: o estado só muda por métodos com nome de negócio.</li>
 *   <li>Regras de negócio: delegadas às specifications ({@link RegrasBoleto}).</li>
 *   <li>Máquina de estados: delegada ao State pattern ({@link EstadoBoleto}).</li>
 * </ul>
 * A entidade é o ponto que GARANTE que ambas sejam aplicadas: não há como emitir, alterar ou
 * receber pagamento sem validar, nem mudar de status sem passar pelo estado atual.
 *
 * <p>Padrão das operações: 1º o ESTADO diz se a ação é permitida (409);
 * 2º as SPECIFICATIONS dizem se os dados são válidos (422); 3º só então o estado muda.
 */
public class Boleto {

    private final BoletoId id;
    private final Instant emitidoEm;
    private DadosBoleto dados;
    private LinhaDigitavel linhaDigitavel;
    private EstadoBoleto estado;
    private Instant notificadoEm;
    private Pagamento pagamento;
    private Motivo motivoCancelamento;

    private Boleto(BoletoId id, Instant emitidoEm, DadosBoleto dados, LinhaDigitavel linhaDigitavel,
                   EstadoBoleto estado, Instant notificadoEm, Pagamento pagamento, Motivo motivoCancelamento) {
        this.id = Objects.requireNonNull(id);
        this.emitidoEm = Objects.requireNonNull(emitidoEm);
        this.dados = Objects.requireNonNull(dados);
        this.linhaDigitavel = Objects.requireNonNull(linhaDigitavel);
        this.estado = Objects.requireNonNull(estado);
        this.notificadoEm = notificadoEm;
        this.pagamento = pagamento;
        this.motivoCancelamento = motivoCancelamento;
    }

    // =============================== criação ===============================

    /** Emite (cria) um boleto. Lança a exceção específica da primeira regra violada. */
    public static Boleto emitir(SolicitacaoBoleto solicitacao) {
        RegrasBoleto.EMISSAO.validar(solicitacao);
        var id = BoletoId.novo();
        var dados = solicitacao.dados();
        return new Boleto(id, solicitacao.momento(), dados, gerarLinha(id, dados),
                EstadoBoleto.inicial(), null, null, null);
    }

    /** Reconstrói a partir da persistência, sem reaplicar regras (o dado já foi validado). */
    public static Boleto reconstituir(BoletoId id, Instant emitidoEm, DadosBoleto dados, LinhaDigitavel linhaDigitavel,
                                      StatusBoleto status, Instant notificadoEm, Pagamento pagamento,
                                      Motivo motivoCancelamento) {
        return new Boleto(id, emitidoEm, dados, linhaDigitavel, EstadoBoleto.de(status),
                notificadoEm, pagamento, motivoCancelamento);
    }

    // =============================== update / delete ===============================

    public void alterar(SolicitacaoBoleto solicitacao) {
        estado.validarAlteracao();
        if (!solicitacao.dados().pagador().equals(dados.pagador())) {
            // erro de PROGRAMAÇÃO (o use case montou a solicitação errada), não de negócio
            throw new IllegalArgumentException("O pagador de um boleto não pode ser alterado");
        }
        RegrasBoleto.ALTERACAO.validar(solicitacao);
        this.dados = solicitacao.dados();
        this.linhaDigitavel = gerarLinha(id, dados); // valor/vencimento mudaram: a linha muda junto
    }

    /** Só verifica: quem remove de fato é o repositório. */
    public void validarExclusao() {
        estado.validarExclusao();
    }

    // =============================== transições (State pattern) ===============================

    /** Registra que o pagador foi avisado. Em NOTIFICADO, é um reenvio (mesmo estado, novo horário). */
    public void registrarNotificacao(Instant momento) {
        estado = estado.notificar();
        notificadoEm = Objects.requireNonNull(momento);
    }

    public void receberPagamento(Pagamento pagamentoInformado, LocalDate hoje) {
        var proximo = estado.receberPagamento();
        RegrasBoleto.RECEBIMENTO.validar(
                new RecebimentoPagamento(dados.valor(), dados.dataVencimento(), pagamentoInformado, hoje));
        estado = proximo;
        pagamento = pagamentoInformado;
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.cancelar();
        motivoCancelamento = motivo;
    }

    private static LinhaDigitavel gerarLinha(BoletoId id, DadosBoleto dados) {
        return LinhaDigitavel.gerar(id, dados.valor(), dados.dataVencimento());
    }

    // =============================== leitura ===============================

    public boolean emAberto() { return estado.emAberto(); }

    public BoletoId getId() { return id; }
    public Instant getEmitidoEm() { return emitidoEm; }
    public DadosBoleto getDados() { return dados; }
    public Pagador getPagador() { return dados.pagador(); }
    public Dinheiro getValor() { return dados.valor(); }
    public LocalDate getDataVencimento() { return dados.dataVencimento(); }
    public LinhaDigitavel getLinhaDigitavel() { return linhaDigitavel; }
    public StatusBoleto getStatus() { return estado.status(); }
    public Optional<Instant> getNotificadoEm() { return Optional.ofNullable(notificadoEm); }
    public Optional<Pagamento> getPagamento() { return Optional.ofNullable(pagamento); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
