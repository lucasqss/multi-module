package com.example.boleto.domain.state;

import com.example.boleto.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern: cada estado do boleto é uma classe que sabe
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
public abstract sealed class EstadoBoleto permits EstadoEmitido, EstadoNotificado, EstadoPago, EstadoCancelado {

    public abstract StatusBoleto status();

    // ---------- transições ----------

    public EstadoBoleto notificar() {
        throw acaoNaoPermitida("notificar");
    }

    public EstadoBoleto receberPagamento() {
        throw acaoNaoPermitida("receber pagamento de");
    }

    public EstadoBoleto cancelar() {
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

    /** Boleto em aberto: ainda pode ser pago. Conta para o limite de boletos por pagador. */
    public boolean emAberto() {
        return false;
    }

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    /** Status persistido -> objeto de estado (usado ao reconstituir do banco). */
    public static EstadoBoleto de(StatusBoleto status) {
        return switch (status) {
            case EMITIDO -> EstadoEmitido.INSTANCIA;
            case NOTIFICADO -> EstadoNotificado.INSTANCIA;
            case PAGO -> EstadoPago.INSTANCIA;
            case CANCELADO -> EstadoCancelado.INSTANCIA;
        };
    }

    public static EstadoBoleto inicial() {
        return EstadoEmitido.INSTANCIA;
    }

    @Override
    public String toString() {
        return status().name();
    }
}
