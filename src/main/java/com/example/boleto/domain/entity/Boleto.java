package com.example.boleto.domain.entity;

import com.example.boleto.domain.specification.boleto.CondicoesEmissao;
import com.example.boleto.domain.specification.boleto.DestinatariosDoBoleto;
import com.example.boleto.domain.specification.boleto.EmissaoBoleto;
import com.example.boleto.domain.specification.boleto.RecebimentoPagamento;
import com.example.boleto.domain.specification.boleto.RegrasBoleto;
import com.example.boleto.domain.state.EstadoBoleto;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagador;
import com.example.boleto.domain.valueobject.Pagamento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root do boleto. Itens e destinatários são PARTES dele: não têm repositório nem
 * caso de uso próprios; toda mudança passa por aqui, que é quem garante as regras do conjunto.
 *
 * <p>Domínio RICO:
 * <ul>
 *   <li>Sem setters: o estado só muda por métodos com nome de negócio.</li>
 *   <li>Regras de negócio: delegadas às specifications ({@link RegrasBoleto}).</li>
 *   <li>Máquina de estados: delegada ao State pattern ({@link EstadoBoleto}).</li>
 * </ul>
 *
 * <p>Ciclo: nasce em EM_EDICAO (rascunho, pode estar incompleto) → as seções são salvas por partes
 * → {@link #emitir} exige completude, gera a linha digitável e o boleto passa a valer.
 *
 * <p>Padrão das operações: 1º o ESTADO diz se a ação é permitida (409);
 * 2º as SPECIFICATIONS dizem se os dados são válidos (422); 3º só então o estado muda.
 */
public class Boleto {

    private final BoletoId id;
    private final Pagador pagador;
    private final Instant criadoEm;
    private LocalDate dataVencimento;
    private ItensBoleto itens;
    private Destinatarios destinatarios;
    private EstadoBoleto estado;
    private LinhaDigitavel linhaDigitavel;
    private Instant emitidoEm;
    private Instant notificadoEm;
    private Pagamento pagamento;
    private Motivo motivoCancelamento;

    private Boleto(BoletoId id, Pagador pagador, Instant criadoEm, LocalDate dataVencimento, ItensBoleto itens,
                   Destinatarios destinatarios, EstadoBoleto estado, LinhaDigitavel linhaDigitavel, Instant emitidoEm,
                   Instant notificadoEm, Pagamento pagamento, Motivo motivoCancelamento) {
        this.id = Objects.requireNonNull(id);
        this.pagador = Objects.requireNonNull(pagador);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.dataVencimento = Objects.requireNonNull(dataVencimento);
        this.itens = Objects.requireNonNull(itens);
        this.destinatarios = Objects.requireNonNull(destinatarios);
        this.estado = Objects.requireNonNull(estado);
        this.linhaDigitavel = linhaDigitavel;
        this.emitidoEm = emitidoEm;
        this.notificadoEm = notificadoEm;
        this.pagamento = pagamento;
        this.motivoCancelamento = motivoCancelamento;
    }

    // =============================== criação ===============================

    /** Cria o rascunho (EM_EDICAO) só com o mínimo. Completude é exigida na emissão, não aqui. */
    public static Boleto criar(Pagador pagador, LocalDate dataVencimento, Instant agora) {
        return new Boleto(BoletoId.novo(), pagador, agora, dataVencimento, ItensBoleto.VAZIO,
                Destinatarios.NENHUM, EstadoBoleto.inicial(), null, null, null, null, null);
    }

    /** Reconstrói a partir da persistência, sem reaplicar regras (o dado já foi validado). */
    public static Boleto reconstituir(BoletoId id, Pagador pagador, Instant criadoEm, LocalDate dataVencimento,
                                      ItensBoleto itens, Destinatarios destinatarios, StatusBoleto status,
                                      LinhaDigitavel linhaDigitavel, Instant emitidoEm, Instant notificadoEm,
                                      Pagamento pagamento, Motivo motivoCancelamento) {
        return new Boleto(id, pagador, criadoEm, dataVencimento, itens, destinatarios, EstadoBoleto.de(status),
                linhaDigitavel, emitidoEm, notificadoEm, pagamento, motivoCancelamento);
    }

    // =============================== seções (só em EM_EDICAO) ===============================

    /** Seção "dados gerais". As regras de vencimento dependem do dia da emissão: valem em {@link #emitir}. */
    public void alterarVencimento(LocalDate novoVencimento) {
        estado.validarEdicao();
        this.dataVencimento = Objects.requireNonNull(novoVencimento);
    }

    /** Seção "itens": substitui a seção inteira. Regras da seção agora; completude só na emissão. */
    public void salvarItens(ItensBoleto novosItens) {
        estado.validarEdicao();
        RegrasBoleto.ITENS.validar(novosItens);
        this.itens = novosItens;
    }

    /** Seção "destinatários" (opcional): substitui a seção inteira. */
    public void salvarDestinatarios(Destinatarios novos) {
        estado.validarEdicao();
        RegrasBoleto.DESTINATARIOS.validar(new DestinatariosDoBoleto(pagador, novos));
        this.destinatarios = novos;
    }

    /** Só verifica: quem remove de fato é o repositório. */
    public void validarExclusao() {
        estado.validarExclusao();
    }

    // =============================== transições (State pattern) ===============================

    /** Finaliza a edição: exige completude, gera a linha digitável. EM_EDICAO -> EMITIDO. */
    public void emitir(CondicoesEmissao condicoes) {
        var proximo = estado.emitir();
        RegrasBoleto.EMISSAO.validar(new EmissaoBoleto(pagador, itens, dataVencimento, condicoes));
        estado = proximo;
        linhaDigitavel = LinhaDigitavel.gerar(id, getValor(), dataVencimento);
        emitidoEm = condicoes.momento();
    }

    /** Registra que o pagador foi avisado. Em NOTIFICADO, é um reenvio (mesmo estado, novo horário). */
    public void registrarNotificacao(Instant momento) {
        estado = estado.notificar();
        notificadoEm = Objects.requireNonNull(momento);
    }

    public void receberPagamento(Pagamento pagamentoInformado, LocalDate hoje) {
        var proximo = estado.receberPagamento();
        RegrasBoleto.RECEBIMENTO.validar(
                new RecebimentoPagamento(getValor(), dataVencimento, pagamentoInformado, hoje));
        estado = proximo;
        pagamento = pagamentoInformado;
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== leitura ===============================

    public boolean emAberto() { return estado.emAberto(); }

    public BoletoId getId() { return id; }
    public Pagador getPagador() { return pagador; }
    public Instant getCriadoEm() { return criadoEm; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public ItensBoleto getItens() { return itens; }
    public Destinatarios getDestinatarios() { return destinatarios; }
    /** Valor do boleto = soma dos itens. */
    public Dinheiro getValor() { return itens.total(); }
    public StatusBoleto getStatus() { return estado.status(); }
    public Optional<LinhaDigitavel> getLinhaDigitavel() { return Optional.ofNullable(linhaDigitavel); }
    public Optional<Instant> getEmitidoEm() { return Optional.ofNullable(emitidoEm); }
    public Optional<Instant> getNotificadoEm() { return Optional.ofNullable(notificadoEm); }
    public Optional<Pagamento> getPagamento() { return Optional.ofNullable(pagamento); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
