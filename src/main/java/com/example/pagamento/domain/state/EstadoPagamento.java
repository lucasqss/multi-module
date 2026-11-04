package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern: cada estado do pagamento é uma classe que sabe (1) quais ações aceita e
 * (2) para qual estado cada ação leva.
 *
 * <p>Por padrão TODA ação é proibida ({@link TransicaoEstadoInvalidaException}, 409); cada estado
 * sobrescreve só o que permite. As transições RETORNAM o próximo estado; a entidade faz
 * {@code estado = estado.acao()}. {@code sealed}: só as classes de {@code permits} são estados; o {@code switch}
 * de {@link #de} sobre o enum, sem {@code default}, não compila se faltar um status.
 */
public abstract sealed class EstadoPagamento
        permits EstadoEmEdicao, EstadoAgendado, EstadoEmProcessamento, EstadoRecusado,
                EstadoPago, EstadoEmAnalise, EstadoCancelado {

    public abstract StatusPagamento status();

    public EstadoPagamento submeter() {
        throw acaoNaoPermitida("submeter");
    }

    public EstadoPagamento enviarAoBanco() {
        throw acaoNaoPermitida("enviar ao banco");
    }

    public EstadoPagamento reenviarAoBanco() {
        throw acaoNaoPermitida("reenviar ao banco");
    }

    public EstadoPagamento cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    /** FATO vindo do banco: liquidado ou recusado; {@code conforme} = passou na conformidade. */
    public EstadoPagamento receberRetornoBanco(boolean liquidado, boolean conforme) {
        throw acaoNaoPermitida("receber retorno do banco para");
    }

    /** Salvar uma seção (boletos, conta, data). */
    public void validarEdicao() {
        throw acaoNaoPermitida("editar");
    }

    public void validarExclusao() {
        throw acaoNaoPermitida("excluir");
    }

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    /** Status persistido -> objeto de estado (usado ao reconstituir do banco). */
    public static EstadoPagamento de(StatusPagamento status) {
        return switch (status) {
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case AGENDADO -> EstadoAgendado.INSTANCIA;
            case EM_PROCESSAMENTO -> EstadoEmProcessamento.INSTANCIA;
            case RECUSADO -> EstadoRecusado.INSTANCIA;
            case PAGO -> EstadoPago.INSTANCIA;
            case EM_ANALISE -> EstadoEmAnalise.INSTANCIA;
            case CANCELADO -> EstadoCancelado.INSTANCIA;
        };
    }

    public static EstadoPagamento inicial() {
        return EstadoEmEdicao.INSTANCIA;
    }

    @Override
    public String toString() {
        return status().name();
    }
}
