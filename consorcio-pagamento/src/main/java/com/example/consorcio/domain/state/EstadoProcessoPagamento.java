package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern: cada estado do processo é uma classe que sabe
 * (1) quais ações aceita e (2) para qual estado cada ação leva.
 *
 * <p>Por padrão TODA ação é proibida (lança {@link TransicaoEstadoInvalidaException});
 * cada estado sobrescreve apenas o que permite. Assim, um estado novo nasce "fechado"
 * e é impossível esquecer de bloquear uma ação.
 *
 * <p>As transições RETORNAM o próximo estado (em vez de alterar a entidade), então
 * a entidade continua sendo a única dona do próprio estado e não precisa de setter.
 *
 * <p>{@code sealed}: o compilador conhece todos os estados, e o {@code switch} em {@link #de}
 * falha a compilação se um novo status for criado sem estado correspondente.
 */
public abstract sealed class EstadoProcessoPagamento
        permits EstadoAberto, EstadoEmProcessamento, EstadoRejeitado, EstadoConfirmado, EstadoCancelado {

    public abstract StatusProcessoPagamento status();

    // ---------- transições ----------

    public EstadoProcessoPagamento enviarParaProcessamento() {
        throw acaoNaoPermitida("enviar para processamento");
    }

    public EstadoProcessoPagamento confirmar() {
        throw acaoNaoPermitida("confirmar");
    }

    public EstadoProcessoPagamento rejeitar() {
        throw acaoNaoPermitida("rejeitar");
    }

    public EstadoProcessoPagamento reabrir() {
        throw acaoNaoPermitida("reabrir");
    }

    public EstadoProcessoPagamento cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    // ---------- ações que não mudam de estado, mas dependem dele ----------

    public void validarAlteracao() {
        throw acaoNaoPermitida("alterar");
    }

    public void validarExclusao() {
        throw acaoNaoPermitida("excluir");
    }

    // ---------- consultas ----------

    /** Processo em andamento bloqueia a abertura de outro para a mesma cota. */
    public boolean emAndamento() {
        return false;
    }

    public boolean finalizado() {
        return false;
    }

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    /** Status persistido -> objeto de estado (usado ao reconstituir do banco). */
    public static EstadoProcessoPagamento de(StatusProcessoPagamento status) {
        return switch (status) {
            case ABERTO -> EstadoAberto.INSTANCIA;
            case EM_PROCESSAMENTO -> EstadoEmProcessamento.INSTANCIA;
            case REJEITADO -> EstadoRejeitado.INSTANCIA;
            case CONFIRMADO -> EstadoConfirmado.INSTANCIA;
            case CANCELADO -> EstadoCancelado.INSTANCIA;
        };
    }

    public static EstadoProcessoPagamento inicial() {
        return EstadoAberto.INSTANCIA;
    }

    @Override
    public String toString() {
        return status().name();
    }
}
