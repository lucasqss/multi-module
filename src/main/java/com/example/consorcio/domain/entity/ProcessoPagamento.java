package com.example.consorcio.domain.entity;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.pagamento.RegrasProcessoPagamento;
import com.example.consorcio.domain.state.EstadoProcessoPagamento;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.Motivo;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root do processo de pagamento de consórcio.
 *
 * <p>Domínio RICO:
 * <ul>
 *   <li>Sem setters: o estado só muda por métodos com nome de negócio.</li>
 *   <li>Regras de negócio: delegadas às specifications ({@link RegrasProcessoPagamento}).</li>
 *   <li>Máquina de estados: delegada ao State pattern ({@link EstadoProcessoPagamento}).</li>
 * </ul>
 * A entidade é o ponto que GARANTE que ambas sejam aplicadas: não há como abrir/alterar
 * sem validar, nem mudar de status sem passar pelo estado atual.
 */
public class ProcessoPagamento {

    private final ProcessoPagamentoId id;
    private final IdentificadorCota cota;
    private final Instant criadoEm;
    private DadosPagamento dados;
    private EstadoProcessoPagamento estado;
    private Motivo motivoRejeicao;
    private Motivo motivoCancelamento;

    private ProcessoPagamento(ProcessoPagamentoId id, IdentificadorCota cota, Instant criadoEm, DadosPagamento dados,
                              EstadoProcessoPagamento estado, Motivo motivoRejeicao, Motivo motivoCancelamento) {
        this.id = Objects.requireNonNull(id);
        this.cota = Objects.requireNonNull(cota);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.dados = Objects.requireNonNull(dados);
        this.estado = Objects.requireNonNull(estado);
        this.motivoRejeicao = motivoRejeicao;
        this.motivoCancelamento = motivoCancelamento;
    }

    // =============================== criação ===============================

    /** Abre um novo processo. Lança a exceção específica da primeira regra violada. */
    public static ProcessoPagamento abrir(SolicitacaoPagamento solicitacao) {
        RegrasProcessoPagamento.ABERTURA.validar(solicitacao);
        return new ProcessoPagamento(ProcessoPagamentoId.novo(), solicitacao.cota().identificador(), solicitacao.momento(),
                solicitacao.dados(), EstadoProcessoPagamento.inicial(), null, null);
    }

    /** Reconstrói a partir da persistência, sem reaplicar regras de criação (o dado já foi validado). */
    public static ProcessoPagamento reconstituir(ProcessoPagamentoId id, IdentificadorCota cota, Instant criadoEm,
                                                 DadosPagamento dados, StatusProcessoPagamento status,
                                                 Motivo motivoRejeicao, Motivo motivoCancelamento) {
        return new ProcessoPagamento(id, cota, criadoEm, dados, EstadoProcessoPagamento.de(status),
                motivoRejeicao, motivoCancelamento);
    }

    // =============================== update / delete ===============================

    /** 1º o ESTADO diz se pode alterar (409); 2º as SPECIFICATIONS dizem se os dados novos valem (422). */
    public void alterar(SolicitacaoPagamento solicitacao) {
        estado.validarAlteracao();
        if (!solicitacao.cota().identificador().equals(cota)) {
            // erro de PROGRAMAÇÃO (o use case montou a solicitação errada), não de negócio
            throw new IllegalArgumentException("Solicitação de outra cota: " + solicitacao.cota().identificador());
        }
        RegrasProcessoPagamento.ALTERACAO.validar(solicitacao);
        this.dados = solicitacao.dados();
    }

    /** Só verifica: quem remove de fato é o repositório. */
    public void validarExclusao() {
        estado.validarExclusao();
    }

    // =============================== transições (State pattern) ===============================

    public void enviarParaProcessamento() {
        estado = estado.enviarParaProcessamento();
    }

    public void confirmar() {
        estado = estado.confirmar();
    }

    public void rejeitar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.rejeitar();
        motivoRejeicao = motivo;
    }

    public void reabrir() {
        estado = estado.reabrir();
        motivoRejeicao = null;
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== leitura ===============================

    public boolean emAndamento() { return estado.emAndamento(); }
    public boolean finalizado() { return estado.finalizado(); }

    public ProcessoPagamentoId getId() { return id; }
    public IdentificadorCota getCota() { return cota; }
    public Instant getCriadoEm() { return criadoEm; }
    public DadosPagamento getDados() { return dados; }
    public Dinheiro getValor() { return dados.valor(); }
    public FormaPagamento getFormaPagamento() { return dados.formaPagamento(); }
    public LocalDate getDataVencimento() { return dados.dataVencimento(); }
    public Optional<ContaDebito> getContaDebito() { return dados.conta(); }
    public StatusProcessoPagamento getStatus() { return estado.status(); }
    public Optional<Motivo> getMotivoRejeicao() { return Optional.ofNullable(motivoRejeicao); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
