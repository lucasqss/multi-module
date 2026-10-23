package com.example.processo.core.motor.domain.state;

/**
 * Padrão State (GoF) num enum: cada situação do processo é um estado que sabe quais eventos aceita e para qual
 * situação cada um leva. O Context é o {@code Processo}, que delega a ela e não tem nenhum {@code if} sobre a
 * situação. A máquina é a mesma para todos os segmentos:
 *
 * <pre>
 * ABERTO ──iniciarProcessamento──► EM_PROCESSAMENTO ──concluir──► CONCLUIDO
 *    │                                    └───recusar(motivo)──► RECUSADO
 *    └──cancelar──► CANCELADO
 * </pre>
 *
 * Por padrão todo evento é inválido; cada constante sobrescreve só as transições que permite. O nome da constante
 * é o valor persistido (PROCESSO.SITUACAO) e serializado.
 */
public enum Situacao {

    /** Recém-criado: pode ir para processamento ou ser cancelado. */
    ABERTO {
        @Override
        public Situacao iniciarProcessamento() {
            return EM_PROCESSAMENTO;
        }

        @Override
        public Situacao cancelar() {
            return CANCELADO;
        }
    },

    /** O segmento está avaliando: termina concluído ou recusado. Não pode mais ser cancelado. */
    EM_PROCESSAMENTO {
        @Override
        public Situacao concluir() {
            return CONCLUIDO;
        }

        @Override
        public Situacao recusar() {
            return RECUSADO;
        }
    },

    /** Finais: não aceitam nenhum evento. */
    CONCLUIDO, RECUSADO, CANCELADO;

    public Situacao iniciarProcessamento() {
        throw new TransicaoInvalidaException(this, "processado");
    }

    public Situacao concluir() {
        throw new TransicaoInvalidaException(this, "concluído");
    }

    public Situacao recusar() {
        throw new TransicaoInvalidaException(this, "recusado");
    }

    public Situacao cancelar() {
        throw new TransicaoInvalidaException(this, "cancelado");
    }
}
