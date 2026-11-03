package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.example.consorcio.domain.enums.StatusProcessoPagamento.ABERTO;
import static com.example.consorcio.domain.enums.StatusProcessoPagamento.CANCELADO;
import static com.example.consorcio.domain.enums.StatusProcessoPagamento.CONFIRMADO;
import static com.example.consorcio.domain.enums.StatusProcessoPagamento.EM_PROCESSAMENTO;
import static com.example.consorcio.domain.enums.StatusProcessoPagamento.REJEITADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testa a máquina de estados como uma TABELA: para cada (estado, ação), ou o destino esperado
 * ou erro. Se alguém mudar uma transição sem querer, este teste acusa.
 */
class EstadoProcessoPagamentoTest {

    private static final Map<String, Function<EstadoProcessoPagamento, EstadoProcessoPagamento>> ACOES = Map.of(
            "enviar", EstadoProcessoPagamento::enviarParaProcessamento,
            "confirmar", EstadoProcessoPagamento::confirmar,
            "rejeitar", EstadoProcessoPagamento::rejeitar,
            "reabrir", EstadoProcessoPagamento::reabrir,
            "cancelar", EstadoProcessoPagamento::cancelar);

    /** Transições PERMITIDAS. Tudo que não está aqui deve falhar. */
    private static final Map<StatusProcessoPagamento, Map<String, StatusProcessoPagamento>> PERMITIDAS = Map.of(
            ABERTO, Map.of("enviar", EM_PROCESSAMENTO, "cancelar", CANCELADO),
            EM_PROCESSAMENTO, Map.of("confirmar", CONFIRMADO, "rejeitar", REJEITADO),
            REJEITADO, Map.of("reabrir", ABERTO, "cancelar", CANCELADO),
            CONFIRMADO, Map.of(),
            CANCELADO, Map.of());

    @Test
    void tabelaDeTransicoes() {
        for (var status : StatusProcessoPagamento.values()) {
            var estado = EstadoProcessoPagamento.de(status);
            assertEquals(status, estado.status());

            ACOES.forEach((acao, executar) -> {
                var destinoEsperado = PERMITIDAS.get(status).get(acao);
                if (destinoEsperado != null) {
                    assertEquals(destinoEsperado, executar.apply(estado).status(), status + " --" + acao + "-->");
                } else {
                    assertThrows(TransicaoEstadoInvalidaException.class, () -> executar.apply(estado),
                            status + " não deveria aceitar " + acao);
                }
            });
        }
    }

    @Test
    void somenteAbertoPermiteAlterarEExcluir() {
        for (var status : StatusProcessoPagamento.values()) {
            var estado = EstadoProcessoPagamento.de(status);
            if (status == ABERTO) {
                estado.validarAlteracao();
                estado.validarExclusao();
            } else {
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarAlteracao);
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarExclusao);
            }
        }
    }

    @Test
    void emAndamentoEFinalizado() {
        var emAndamento = Set.of(ABERTO, EM_PROCESSAMENTO, REJEITADO);
        var finalizados = Set.of(CONFIRMADO, CANCELADO);
        for (var status : StatusProcessoPagamento.values()) {
            var estado = EstadoProcessoPagamento.de(status);
            assertEquals(emAndamento.contains(status), estado.emAndamento(), status.name());
            assertEquals(finalizados.contains(status), estado.finalizado(), status.name());
        }
    }
}
