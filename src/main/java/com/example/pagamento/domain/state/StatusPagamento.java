package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern em ENUM: cada status sabe (1) quais ações aceita e (2) para qual status cada ação leva.
 * Por padrão TODA ação é proibida ({@link TransicaoEstadoInvalidaException}, 409); cada constante
 * sobrescreve só o que permite. A entidade faz {@code status = status.acao()}. A máquina inteira cabe aqui:
 *
 * <pre>
 *  EM_EDICAO ─submeter─▶ AGENDADO ─enviar─▶ EM_PROCESSAMENTO ── retorno: liquidado conforme ──▶ PAGO
 *                                                 ├── retorno: recusado ──▶ RECUSADO ─reenviar─┐
 *                                                 │       ▲────────────────────────────────────┘
 *                                                 └── retorno: liquidado ≠ total ──▶ EM_ANALISE
 *  cancelar: EM_EDICAO, AGENDADO, RECUSADO e EM_ANALISE ──▶ CANCELADO
 * </pre>
 *
 * Classe por estado só valeria se um estado tivesse dados ou dependências próprios.
 */
public enum StatusPagamento {

    /** Rascunho: seções salvas por partes, pode estar incompleto e pode ser excluído. */
    EM_EDICAO {
        @Override public StatusPagamento submeter() { return AGENDADO; }
        @Override public StatusPagamento cancelar() { return CANCELADO; }
        @Override public void validarEdicao() { }
        @Override public void validarExclusao() { }
    },

    /** Completo e validado: aguarda o envio ao banco (na vida real, um job na data de pagamento). */
    AGENDADO {
        @Override public StatusPagamento enviarAoBanco() { return EM_PROCESSAMENTO; }
        @Override public StatusPagamento cancelar() { return CANCELADO; }
    },

    /** Enviado ao banco: aguarda o retorno. O usuário não cancela (o dinheiro pode já ter saído). */
    EM_PROCESSAMENTO {
        /** Recusado: RECUSADO. Liquidado conforme: PAGO. Liquidado divergente: análise (fato, não se recusa). */
        @Override
        public StatusPagamento receberRetornoBanco(boolean liquidado, boolean conforme) {
            if (!liquidado) {
                return RECUSADO;
            }
            return conforme ? PAGO : EM_ANALISE;
        }
    },

    /** O banco recusou (saldo, conta...): pode ser reenviado ou cancelado. */
    RECUSADO {
        @Override public StatusPagamento reenviarAoBanco() { return EM_PROCESSAMENTO; }
        @Override public StatusPagamento cancelar() { return CANCELADO; }
    },

    /** Final de sucesso. */
    PAGO,

    /** Liquidado com valor diferente do total: aguarda decisão humana; aqui, só sai por cancelamento. */
    EM_ANALISE {
        @Override public StatusPagamento cancelar() { return CANCELADO; }
    },

    /** Final. */
    CANCELADO;

    public StatusPagamento submeter() {
        throw acaoNaoPermitida("submeter");
    }

    public StatusPagamento enviarAoBanco() {
        throw acaoNaoPermitida("enviar ao banco");
    }

    public StatusPagamento reenviarAoBanco() {
        throw acaoNaoPermitida("reenviar ao banco");
    }

    public StatusPagamento cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    /** FATO vindo do banco: liquidado ou recusado; {@code conforme} = passou na conformidade. */
    public StatusPagamento receberRetornoBanco(boolean liquidado, boolean conforme) {
        throw acaoNaoPermitida("receber retorno do banco para");
    }

    /** Salvar uma seção (boletos, conta, data). */
    public void validarEdicao() {
        throw acaoNaoPermitida("editar");
    }

    public void validarExclusao() {
        throw acaoNaoPermitida("excluir");
    }

    private TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(this, acao);
    }
}
