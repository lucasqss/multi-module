package com.example.consorcio.domain.entity;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root do processo de pagamento de consórcio.
 *
 * <p>NÃO é anêmica: não existem setters públicos. Todo estado só muda por métodos com
 * significado de negócio ({@link #abrir}, {@link #confirmar}, {@link #cancelar}), e cada um
 * protege as próprias invariantes. É impossível existir um ProcessoPagamento inválido em memória.
 */
public class ProcessoPagamento {

    private final ProcessoPagamentoId id;
    private final IdentificadorCota cota;
    private final Dinheiro valor;
    private final FormaPagamento formaPagamento;
    private final ContaDebito contaDebito;
    private final LocalDate dataVencimento;
    private final Instant criadoEm;
    private StatusProcessoPagamento status;
    private String motivoCancelamento;

    private ProcessoPagamento(ProcessoPagamentoId id, IdentificadorCota cota, Dinheiro valor,
                              FormaPagamento formaPagamento, ContaDebito contaDebito,
                              LocalDate dataVencimento, Instant criadoEm,
                              StatusProcessoPagamento status, String motivoCancelamento) {
        this.id = Objects.requireNonNull(id);
        this.cota = Objects.requireNonNull(cota);
        this.valor = Objects.requireNonNull(valor);
        this.formaPagamento = Objects.requireNonNull(formaPagamento);
        this.contaDebito = contaDebito;
        this.dataVencimento = Objects.requireNonNull(dataVencimento);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.status = Objects.requireNonNull(status);
        this.motivoCancelamento = motivoCancelamento;
    }

    /**
     * Cria um novo processo, validando as invariantes que dependem apenas dos próprios dados.
     * Regras que dependem de OUTROS objetos (a cota) ficam na factory/specifications.
     */
    public static ProcessoPagamento abrir(IdentificadorCota cota, Dinheiro valor, FormaPagamento formaPagamento,
                                          ContaDebito contaDebito, LocalDate dataVencimento,
                                          Instant agora, LocalDate hoje, int prazoMaximoVencimentoDias) {
        Objects.requireNonNull(formaPagamento, "forma de pagamento é obrigatória");
        Objects.requireNonNull(dataVencimento, "data de vencimento é obrigatória");

        if (!valor.ehPositivo()) {
            throw new RegraNegocioException("VALOR_NAO_POSITIVO", "Valor do pagamento deve ser maior que zero");
        }
        if (dataVencimento.isBefore(hoje)) {
            throw new RegraNegocioException("VENCIMENTO_NO_PASSADO", "Data de vencimento não pode estar no passado");
        }
        if (dataVencimento.isAfter(hoje.plusDays(prazoMaximoVencimentoDias))) {
            throw new RegraNegocioException("VENCIMENTO_ALEM_DO_PRAZO",
                    "Data de vencimento deve ser em até %d dias".formatted(prazoMaximoVencimentoDias));
        }
        if (formaPagamento.exigeContaDebito() && contaDebito == null) {
            throw new RegraNegocioException("CONTA_DEBITO_OBRIGATORIA",
                    "Conta para débito é obrigatória quando a forma de pagamento é " + formaPagamento);
        }
        if (!formaPagamento.exigeContaDebito() && contaDebito != null) {
            throw new RegraNegocioException("CONTA_DEBITO_INDEVIDA",
                    "Conta para débito só deve ser informada para " + FormaPagamento.DEBITO_EM_CONTA);
        }

        return new ProcessoPagamento(ProcessoPagamentoId.novo(), cota, valor, formaPagamento, contaDebito,
                dataVencimento, agora, StatusProcessoPagamento.ABERTO, null);
    }

    /**
     * Reconstrói um processo já existente (ex.: vindo do banco). Não reaplica regras de criação,
     * pois o estado já foi validado quando o processo nasceu.
     */
    public static ProcessoPagamento reconstituir(ProcessoPagamentoId id, IdentificadorCota cota, Dinheiro valor,
                                                 FormaPagamento formaPagamento, ContaDebito contaDebito,
                                                 LocalDate dataVencimento, Instant criadoEm,
                                                 StatusProcessoPagamento status, String motivoCancelamento) {
        return new ProcessoPagamento(id, cota, valor, formaPagamento, contaDebito,
                dataVencimento, criadoEm, status, motivoCancelamento);
    }

    public void confirmar() {
        transicionarPara(StatusProcessoPagamento.CONFIRMADO);
    }

    public void cancelar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("MOTIVO_OBRIGATORIO", "Motivo do cancelamento é obrigatório");
        }
        transicionarPara(StatusProcessoPagamento.CANCELADO);
        this.motivoCancelamento = motivo.strip();
    }

    public boolean estaEmAberto() {
        return status.emAberto();
    }

    private void transicionarPara(StatusProcessoPagamento destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new RegraNegocioException("TRANSICAO_INVALIDA",
                    "Processo %s não pode ir de %s para %s".formatted(id, status, destino));
        }
        this.status = destino;
    }

    // Somente getters: leitura livre, escrita só via comportamento.
    public ProcessoPagamentoId getId() { return id; }
    public IdentificadorCota getCota() { return cota; }
    public Dinheiro getValor() { return valor; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public Optional<ContaDebito> getContaDebito() { return Optional.ofNullable(contaDebito); }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public Instant getCriadoEm() { return criadoEm; }
    public StatusProcessoPagamento getStatus() { return status; }
    public Optional<String> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
