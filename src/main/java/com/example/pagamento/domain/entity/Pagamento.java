package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.specification.pagamento.RegrasPagamento;
import com.example.pagamento.domain.specification.pagamento.RetornoPagamento;
import com.example.pagamento.domain.specification.pagamento.SubmissaoPagamento;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.IdTransacao;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.RetornoBanco;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root: pagamento de um ou mais boletos. O boleto em si é de um sistema externo (o emissor);
 * aqui ficam só cópias dos dados ({@link BoletoAPagar}).
 *
 * <p>Domínio RICO e entidade JPA ao mesmo tempo: campos privados (acesso por campo), construtor sem
 * argumentos só para o JPA, nenhum setter. O estado só muda por métodos de negócio, sempre nesta ordem:
 * 1º o STATUS diz se a ação é permitida (409); 2º as REGRAS validam (422) ou, para fatos externos,
 * classificam; 3º só então muda. A entidade carregada é gerenciada: o UPDATE sai no commit; se algo
 * lançar, o caso de uso faz rollback. As regras que variam por {@link Modalidade} ficam na cadeia de
 * {@link RegrasPagamento}, não em {@code if}s aqui.
 */
@Entity
@Table(name = "pagamento", indexes = @Index(name = "ux_pagamento_transacao", columnList = "id_transacao", unique = true))
public class Pagamento {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    /**
     * Seção "boletos". LAZY porque FOR UPDATE não combina com join de coleção; {@code @BatchSize}: na listagem,
     * os boletos de vários pagamentos vêm numa consulta só (o padrão do Quarkus é 16; 100 = página máxima).
     * Coleção gerenciada: nunca trocar a lista, só limpar e repovoar.
     */
    @ElementCollection
    @CollectionTable(name = "pagamento_boleto", joinColumns = @JoinColumn(name = "pagamento_id"))
    @OrderColumn(name = "ordem")
    @BatchSize(size = 100)
    private List<BoletoAPagar> boletos = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "agencia", column = @Column(name = "agencia_debito", length = 4))
    @AttributeOverride(name = "conta", column = @Column(name = "conta_debito", length = 12))
    private ContaDebito contaDebito;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    /** State pattern: o enum tem o comportamento de cada status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(nullable = false)
    private int envios;

    @Column(name = "id_transacao", length = 50)
    private IdTransacao idTransacao;

    @Column(length = 60)
    private String autenticacao;

    @Column(name = "motivo_recusa")
    private String motivoRecusa;

    @Embedded
    @AttributeOverride(name = "codigo", column = @Column(name = "motivo_analise_codigo", length = 60))
    @AttributeOverride(name = "mensagem", column = @Column(name = "motivo_analise_mensagem"))
    private MotivoAnalise motivoAnalise;

    @Column(name = "motivo_cancelamento")
    private Motivo motivoCancelamento;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    /** Só para o JPA. */
    protected Pagamento() {
    }

    private Pagamento(Modalidade modalidade, Instant criadoEm) {
        this.id = UUID.randomUUID();
        this.modalidade = Objects.requireNonNull(modalidade);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.status = StatusPagamento.EM_EDICAO;
    }

    // =============================== criação ===============================

    /** Cria o rascunho (EM_EDICAO) só com a modalidade. Completude é exigida em {@link #submeter}. */
    public static Pagamento criar(Modalidade modalidade, Instant agora) {
        return new Pagamento(modalidade, agora);
    }

    // =============================== edição por partes (só em EM_EDICAO) ===============================

    /** Seção "boletos": substitui inteira. Só as regras da seção; o pagamento pode continuar incompleto. */
    public void salvarBoletos(BoletosDoPagamento novos) {
        status.validarEdicao();
        RegrasPagamento.BOLETOS.validar(novos);
        boletos.clear();
        boletos.addAll(novos.boletos());
    }

    /** Seção "dados gerais". Regras que dependem de "hoje" valem na submissão. */
    public void alterarDadosGerais(ContaDebito novaConta, LocalDate novaData) {
        status.validarEdicao();
        this.contaDebito = novaConta;
        this.dataPagamento = novaData;
    }

    public void validarExclusao() {
        status.validarExclusao();
    }

    // =============================== transições (comandos do usuário) ===============================

    /** Finaliza a edição: completude + regras da modalidade (Chain of Responsibility). EM_EDICAO -> AGENDADO. */
    public void submeter(LocalDate hoje) {
        var proximo = status.submeter();
        RegrasPagamento.SUBMISSAO.validar(new SubmissaoPagamento(modalidade, getBoletos(), contaDebito, dataPagamento, hoje));
        status = proximo;
    }

    /** AGENDADO -> EM_PROCESSAMENTO, com uma nova transação (o id é nosso; vai ao banco e volta no retorno). */
    public void enviarAoBanco() {
        status = status.enviarAoBanco();
        novaTransacao();
    }

    /** RECUSADO -> EM_PROCESSAMENTO, com outra transação. */
    public void reenviarAoBanco() {
        status = status.reenviarAoBanco();
        novaTransacao();
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        status = status.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== fato externo (retorno do banco) ===============================

    /**
     * O banco informa o resultado. É um FATO: a conformidade CLASSIFICA (não lança).
     * Idempotente: retorno repetido, ou de uma transação que não é a atual (envio antigo), é ignorado.
     */
    public void registrarRetornoBanco(RetornoBanco retorno) {
        if (!retorno.idTransacao().equals(idTransacao) || autenticacao != null || motivoRecusa != null) {
            return; // aviso repetido ou de outra transação: já processado
        }
        var divergencia = RegrasPagamento.CONFORMIDADE_RETORNO
                .primeiraViolacao(new RetornoPagamento(getValorTotal(), retorno));

        status = status.receberRetornoBanco(retorno.liquidado(), divergencia.isEmpty());
        autenticacao = retorno.codigoAutenticacao();
        motivoRecusa = retorno.motivoRecusa();
        motivoAnalise = divergencia.map(MotivoAnalise::de).orElse(null);
    }

    private void novaTransacao() {
        envios++;
        idTransacao = new IdTransacao(id, envios);
        autenticacao = null;
        motivoRecusa = null;
    }

    // =============================== leitura ===============================

    public UUID getId() { return id; }
    public Modalidade getModalidade() { return modalidade; }
    public Instant getCriadoEm() { return criadoEm; }
    public BoletosDoPagamento getBoletos() { return new BoletosDoPagamento(boletos); }
    /** Valor do pagamento = soma dos boletos. */
    public Dinheiro getValorTotal() { return getBoletos().total(); }
    public Optional<ContaDebito> getContaDebito() { return Optional.ofNullable(contaDebito); }
    public Optional<LocalDate> getDataPagamento() { return Optional.ofNullable(dataPagamento); }
    public StatusPagamento getStatus() { return status; }
    public int getEnvios() { return envios; }
    public Optional<IdTransacao> getIdTransacao() { return Optional.ofNullable(idTransacao); }
    public Optional<String> getAutenticacao() { return Optional.ofNullable(autenticacao); }
    public Optional<String> getMotivoRecusa() { return Optional.ofNullable(motivoRecusa); }
    public Optional<MotivoAnalise> getMotivoAnalise() { return Optional.ofNullable(motivoAnalise); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
