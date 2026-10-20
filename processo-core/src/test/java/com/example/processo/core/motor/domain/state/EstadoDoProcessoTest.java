package com.example.processo.core.motor.domain.state;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A máquina inteira em tabela: para cada estado, cada evento leva a um estado ou é recusado. */
class EstadoDoProcessoTest {

    enum Evento {
        INICIAR_PROCESSAMENTO(EstadoDoProcesso::iniciarProcessamento, "processado"),
        CONCLUIR(EstadoDoProcesso::concluir, "concluído"),
        RECUSAR(EstadoDoProcesso::recusar, "recusado"),
        CANCELAR(EstadoDoProcesso::cancelar, "cancelado");

        final UnaryOperator<EstadoDoProcesso> aplicar;
        final String acao;

        Evento(UnaryOperator<EstadoDoProcesso> aplicar, String acao) {
            this.aplicar = aplicar;
            this.acao = acao;
        }
    }

    /** As únicas transições válidas; todo o resto deve ser recusado. */
    static final Map<Situacao, Map<Evento, Situacao>> TRANSICOES = Map.of(
            Situacao.ABERTO, Map.of(
                    Evento.INICIAR_PROCESSAMENTO, Situacao.EM_PROCESSAMENTO,
                    Evento.CANCELAR, Situacao.CANCELADO),
            Situacao.EM_PROCESSAMENTO, Map.of(
                    Evento.CONCLUIR, Situacao.CONCLUIDO,
                    Evento.RECUSAR, Situacao.RECUSADO),
            Situacao.CONCLUIDO, Map.of(),
            Situacao.RECUSADO, Map.of(),
            Situacao.CANCELADO, Map.of());

    @ParameterizedTest
    @EnumSource(Situacao.class)
    void cadaEstadoAceitaSoAsProprias(Situacao situacao) {
        var estado = EstadoDoProcesso.de(situacao);
        assertEquals(situacao, estado.situacao());

        for (var evento : Evento.values()) {
            var destino = TRANSICOES.get(situacao).get(evento);
            if (destino != null) {
                assertSame(EstadoDoProcesso.de(destino), evento.aplicar.apply(estado), situacao + " + " + evento);
            } else {
                var erro = assertThrows(TransicaoInvalidaException.class, () -> evento.aplicar.apply(estado),
                        situacao + " + " + evento);
                assertEquals("Processo " + situacao + " não pode ser " + evento.acao, erro.getMessage());
            }
        }
    }
}
