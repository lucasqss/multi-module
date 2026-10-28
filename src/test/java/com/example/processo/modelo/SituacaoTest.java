package com.example.processo.modelo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A máquina inteira em tabela: para cada situação, cada evento leva a outra situação ou é recusado. */
class SituacaoTest {

    enum Evento {
        INICIAR_PROCESSAMENTO(Situacao::iniciarProcessamento, "processado"),
        CONCLUIR(Situacao::concluir, "concluído"),
        RECUSAR(Situacao::recusar, "recusado"),
        CANCELAR(Situacao::cancelar, "cancelado");

        final UnaryOperator<Situacao> aplicar;
        final String acao;

        Evento(UnaryOperator<Situacao> aplicar, String acao) {
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
    void cadaSituacaoAceitaSoAsProprias(Situacao situacao) {
        for (var evento : Evento.values()) {
            var destino = TRANSICOES.get(situacao).get(evento);
            if (destino != null) {
                assertEquals(destino, evento.aplicar.apply(situacao), situacao + " + " + evento);
            } else {
                var erro = assertThrows(TransicaoInvalidaException.class, () -> evento.aplicar.apply(situacao),
                        situacao + " + " + evento);
                assertEquals("Processo " + situacao + " não pode ser " + evento.acao, erro.getMessage());
            }
        }
    }
}
