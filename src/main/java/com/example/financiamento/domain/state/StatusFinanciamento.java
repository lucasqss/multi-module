package com.example.financiamento.domain.state;

import com.example.financiamento.domain.exception.TransicaoEstadoInvalidaException;

/**
 * State pattern em ENUM: cada status sabe (1) quais ações aceita e (2) para qual status cada ação leva.
 * Por padrão TODA ação é proibida ({@link TransicaoEstadoInvalidaException}, 409); cada constante
 * sobrescreve só o que permite. A entidade faz {@code status = status.acao()}. A máquina inteira cabe aqui
 * e é a MESMA para imóvel e veículo (o que muda por tipo são as regras e o órgão de registro):
 *
 * <pre>
 *  EM_EDICAO ─submeter─▶ APROVADO ─contratar─▶ EM_REGISTRO ── retorno: registrado conforme ──▶ CONTRATADO
 *                                                   ├── retorno: recusado ──▶ REGISTRO_RECUSADO ─reenviar─┐
 *                                                   │       ▲─────────────────────────────────────────────┘
 *                                                   └── retorno: registrado com divergência ──▶ EM_PENDENCIA
 *  cancelar: EM_EDICAO, APROVADO, REGISTRO_RECUSADO e EM_PENDENCIA ──▶ CANCELADO
 * </pre>
 *
 * Classe por estado só valeria se um estado tivesse dados ou dependências próprios.
 */
public enum StatusFinanciamento {

    /** Rascunho: seções salvas por partes, pode estar incompleto e pode ser excluído. */
    EM_EDICAO {
        @Override public StatusFinanciamento submeter() { return APROVADO; }
        @Override public StatusFinanciamento cancelar() { return CANCELADO; }
        @Override public void validarEdicao() { }
        @Override public void validarExclusao() { }
    },

    /** Passou nas regras de crédito da submissão (aprovação automática, simplificada): aguarda a contratação. */
    APROVADO {
        @Override public StatusFinanciamento contratar() { return EM_REGISTRO; }
        @Override public StatusFinanciamento cancelar() { return CANCELADO; }
    },

    /** Garantia enviada ao órgão (cartório ou trânsito): aguarda o retorno. O usuário não cancela. */
    EM_REGISTRO {
        /** Recusado: REGISTRO_RECUSADO. Registrado conforme: CONTRATADO. Divergente: pendência (fato, não se recusa). */
        @Override
        public StatusFinanciamento receberRetornoRegistro(boolean registrado, boolean conforme) {
            if (!registrado) {
                return REGISTRO_RECUSADO;
            }
            return conforme ? CONTRATADO : EM_PENDENCIA;
        }
    },

    /** O órgão recusou o registro: pode ser reenviado ou cancelado. */
    REGISTRO_RECUSADO {
        @Override public StatusFinanciamento reenviarRegistro() { return EM_REGISTRO; }
        @Override public StatusFinanciamento cancelar() { return CANCELADO; }
    },

    /** Final de sucesso: garantia registrada, recursos liberados. */
    CONTRATADO,

    /** Registrado com divergência: aguarda decisão humana; aqui, só sai por cancelamento. */
    EM_PENDENCIA {
        @Override public StatusFinanciamento cancelar() { return CANCELADO; }
    },

    /** Final. */
    CANCELADO;

    public StatusFinanciamento submeter() {
        throw acaoNaoPermitida("submeter");
    }

    public StatusFinanciamento contratar() {
        throw acaoNaoPermitida("contratar");
    }

    public StatusFinanciamento reenviarRegistro() {
        throw acaoNaoPermitida("reenviar o registro de");
    }

    public StatusFinanciamento cancelar() {
        throw acaoNaoPermitida("cancelar");
    }

    /** FATO vindo do órgão: registrado ou recusado; {@code conforme} = passou na conformidade. */
    public StatusFinanciamento receberRetornoRegistro(boolean registrado, boolean conforme) {
        throw acaoNaoPermitida("receber retorno do registro para");
    }

    /** Salvar uma seção (dados gerais, garantia, documentos). */
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
