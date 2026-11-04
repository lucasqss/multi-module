package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern: cada estado do processo é uma classe que sabe (1) quais ações aceita e
 * (2) para qual estado cada ação leva.
 *
 * <p>Por padrão TODA ação é proibida (lança {@link TransicaoEstadoInvalidaException}, 409);
 * cada estado sobrescreve apenas o que permite. Um estado novo nasce "fechado".
 *
 * <p>As transições RETORNAM o próximo estado; a entidade faz {@code estado = estado.acao()}.
 * Algumas recebem o resultado de uma avaliação (alçada atingida? pagamento conforme?) e o estado
 * escolhe o destino com ele.
 *
 * <p>{@code sealed}: o {@code switch} em {@link #de} não compila se faltar um estado.
 */
public abstract sealed class EstadoProcesso
        permits EstadoEmEdicao, EstadoAguardandoAprovacao, EstadoRejeitado, EstadoAprovado,
                EstadoEmProcessamento, EstadoFalhou, EstadoPago, EstadoEmAnalise, EstadoCancelado {

    public abstract StatusProcesso status();

    // ---------- comandos do usuário ----------

    public EstadoProcesso submeter() {
        throw acaoNaoPermitida("submeter");
    }

    /** {@code alcadaAtingida}: com esta aprovação, o processo tem as aprovações que o valor exige. */
    public EstadoProcesso registrarAprovacao(boolean alcadaAtingida) {
        throw acaoNaoPermitida("aprovar");
    }

    public EstadoProcesso rejeitar() {
        throw acaoNaoPermitida("rejeitar");
    }

    public EstadoProcesso reabrir() {
        throw acaoNaoPermitida("reabrir");
    }

    public EstadoProcesso enviarParaPagamento() {
        throw acaoNaoPermitida("enviar para pagamento");
    }

    public EstadoProcesso retentarPagamento() {
        throw acaoNaoPermitida("retentar o pagamento de");
    }

    public EstadoProcesso cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    // ---------- fatos vindos de sistemas externos ----------

    /** Retorno do banco: liquidado ou recusado; {@code conforme} = passou na conformidade. */
    public EstadoProcesso receberRetornoBanco(boolean liquidado, boolean conforme) {
        throw acaoNaoPermitida("receber retorno do banco para");
    }

    /** O emissor cancelou um dos boletos do processo. */
    public EstadoProcesso boletoCanceladoNoEmissor() {
        throw acaoNaoPermitida("registrar boleto cancelado em");
    }

    // ---------- ações que não mudam o estado, mas dependem dele ----------

    /** Salvar qualquer seção (boletos, conta, data). */
    public void validarEdicao() {
        throw acaoNaoPermitida("editar");
    }

    public void validarExclusao() {
        throw acaoNaoPermitida("excluir");
    }

    // ---------- consultas ----------

    /** Precisa de decisão humana (o motivo fica em {@code motivoAnalise}). */
    public boolean emAnalise() {
        return false;
    }

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    /** Status persistido -> objeto de estado (usado ao reconstituir do banco). */
    public static EstadoProcesso de(StatusProcesso status) {
        return switch (status) {
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case AGUARDANDO_APROVACAO -> EstadoAguardandoAprovacao.INSTANCIA;
            case REJEITADO -> EstadoRejeitado.INSTANCIA;
            case APROVADO -> EstadoAprovado.INSTANCIA;
            case EM_PROCESSAMENTO -> EstadoEmProcessamento.INSTANCIA;
            case FALHOU -> EstadoFalhou.INSTANCIA;
            case PAGO -> EstadoPago.INSTANCIA;
            case EM_ANALISE -> EstadoEmAnalise.INSTANCIA;
            case CANCELADO -> EstadoCancelado.INSTANCIA;
        };
    }

    public static EstadoProcesso inicial() {
        return EstadoEmEdicao.INSTANCIA;
    }

    @Override
    public String toString() {
        return status().name();
    }
}
