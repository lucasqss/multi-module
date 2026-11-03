package com.example.boleto.domain.state;

import com.example.boleto.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.example.boleto.domain.state.StatusBoleto.CANCELADO;
import static com.example.boleto.domain.state.StatusBoleto.EMITIDO;
import static com.example.boleto.domain.state.StatusBoleto.NOTIFICADO;
import static com.example.boleto.domain.state.StatusBoleto.PAGO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testa a máquina de estados como uma TABELA: para cada (estado, ação), ou o destino esperado
 * ou erro. Se alguém mudar uma transição sem querer, este teste acusa.
 */
class EstadoBoletoTest {

    private static final Map<String, Function<EstadoBoleto, EstadoBoleto>> ACOES = Map.of(
            "notificar", EstadoBoleto::notificar,
            "receberPagamento", EstadoBoleto::receberPagamento,
            "cancelar", EstadoBoleto::cancelar);

    /** Transições PERMITIDAS. Tudo que não está aqui deve falhar. */
    private static final Map<StatusBoleto, Map<String, StatusBoleto>> PERMITIDAS = Map.of(
            EMITIDO, Map.of("notificar", NOTIFICADO, "receberPagamento", PAGO, "cancelar", CANCELADO),
            NOTIFICADO, Map.of("notificar", NOTIFICADO, "receberPagamento", PAGO, "cancelar", CANCELADO),
            PAGO, Map.of(),
            CANCELADO, Map.of());

    @Test
    void tabelaDeTransicoes() {
        for (var status : StatusBoleto.values()) {
            var estado = EstadoBoleto.de(status);
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
    void somenteEmitidoPermiteAlterarEExcluir() {
        for (var status : StatusBoleto.values()) {
            var estado = EstadoBoleto.de(status);
            if (status == EMITIDO) {
                estado.validarAlteracao();
                estado.validarExclusao();
            } else {
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarAlteracao);
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarExclusao);
            }
        }
    }

    @Test
    void emAberto() {
        var emAberto = Set.of(EMITIDO, NOTIFICADO);
        for (var status : StatusBoleto.values()) {
            assertEquals(emAberto.contains(status), EstadoBoleto.de(status).emAberto(), status.name());
        }
    }
}
