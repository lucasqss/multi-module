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
public abstract sealed class EstadoBoleto permits EstadoEmEdicao, EstadoEmitido, EstadoNotificado, EstadoPago, EstadoEmAnalise, EstadoDevolvido, EstadoCancelado {

    public abstract StatusBoleto status();

    // ---------- transições ----------

    /** Finaliza a edição: o boleto fica completo, ganha linha digitável e passa a valer. */
    public EstadoBoleto emitir() {
        throw acaoNaoPermitida("emitir");
    }

    public EstadoBoleto notificar() {
        throw acaoNaoPermitida("notificar");
    }

    /**
     * Pagamento recebido do sistema externo (um FATO). {@code conforme} = passou nas regras de conformidade.
     * Cada estado decide o destino: quitar, mandar para análise, ou recusar (409) se não faz sentido.
     */
    public EstadoBoleto receberPagamento(boolean conforme) {
        throw acaoNaoPermitida("receber pagamento de");
    }

    /** Financeiro aceita o pagamento divergente como quitação. */
    public EstadoBoleto aceitarPagamento() {
        throw acaoNaoPermitida("aceitar pagamento de");
    }

    /** Financeiro decide devolver o pagamento divergente ao pagador. */
    public EstadoBoleto devolverPagamento() {
        throw acaoNaoPermitida("devolver pagamento de");
    }

    public EstadoBoleto cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    // ---------- ações que não mudam de estado, mas dependem dele ----------

    /** Salvar qualquer seção (vencimento, itens, destinatários). */
    public void validarEdicao() {
        throw acaoNaoPermitida("editar");
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
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case EMITIDO -> EstadoEmitido.INSTANCIA;
            case NOTIFICADO -> EstadoNotificado.INSTANCIA;
            case PAGO -> EstadoPago.INSTANCIA;
            case EM_ANALISE -> EstadoEmAnalise.INSTANCIA;
            case DEVOLVIDO -> EstadoDevolvido.INSTANCIA;
            case CANCELADO -> EstadoCancelado.INSTANCIA;
        };
    }

    public static EstadoBoleto inicial() {
        return EstadoEmEdicao.INSTANCIA;
    }

    @Override
    public String toString() {
        return status().name();
    }
}
