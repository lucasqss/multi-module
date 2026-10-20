package com.example.processo.core.motor.domain.estado;

/**
 * Padrão State (GoF): cada situação do processo é um objeto que sabe quais eventos aceita e para qual estado
 * cada um leva. O Context é o {@code Processo}, que delega a este objeto e não tem nenhum {@code if} sobre a
 * situação. A máquina é a mesma para todos os segmentos:
 *
 * <pre>
 * ABERTO ──iniciarProcessamento──► EM_PROCESSAMENTO ──concluir──► CONCLUIDO
 *    │                                    └───recusar(motivo)──► RECUSADO
 *    └──cancelar──► CANCELADO
 * </pre>
 *
 * Por padrão todo evento é inválido; cada estado concreto sobrescreve só as transições que permite.
 */
public sealed interface EstadoDoProcesso permits Aberto, EmProcessamento, Concluido, Recusado, Cancelado {

    EstadoDoProcesso ABERTO = new Aberto();
    EstadoDoProcesso EM_PROCESSAMENTO = new EmProcessamento();
    EstadoDoProcesso CONCLUIDO = new Concluido();
    EstadoDoProcesso RECUSADO = new Recusado();
    EstadoDoProcesso CANCELADO = new Cancelado();

    static EstadoDoProcesso de(Situacao situacao) {
        return switch (situacao) {
            case ABERTO -> ABERTO;
            case EM_PROCESSAMENTO -> EM_PROCESSAMENTO;
            case CONCLUIDO -> CONCLUIDO;
            case RECUSADO -> RECUSADO;
            case CANCELADO -> CANCELADO;
        };
    }

    Situacao situacao();

    default EstadoDoProcesso iniciarProcessamento() {
        throw new TransicaoInvalidaException(situacao(), "processado");
    }

    default EstadoDoProcesso concluir() {
        throw new TransicaoInvalidaException(situacao(), "concluído");
    }

    default EstadoDoProcesso recusar() {
        throw new TransicaoInvalidaException(situacao(), "recusado");
    }

    default EstadoDoProcesso cancelar() {
        throw new TransicaoInvalidaException(situacao(), "cancelado");
    }
}
