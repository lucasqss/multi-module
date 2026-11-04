package com.example.boleto.domain.state;

import com.example.boleto.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.example.boleto.domain.state.StatusBoleto.CANCELADO;
import static com.example.boleto.domain.state.StatusBoleto.DEVOLVIDO;
import static com.example.boleto.domain.state.StatusBoleto.EMITIDO;
import static com.example.boleto.domain.state.StatusBoleto.EM_ANALISE;
import static com.example.boleto.domain.state.StatusBoleto.EM_EDICAO;
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
            "emitir", EstadoBoleto::emitir,
            "notificar", EstadoBoleto::notificar,
            "pagamentoConforme", e -> e.receberPagamento(true),
            "pagamentoDivergente", e -> e.receberPagamento(false),
            "aceitarPagamento", EstadoBoleto::aceitarPagamento,
            "devolverPagamento", EstadoBoleto::devolverPagamento,
            "cancelar", EstadoBoleto::cancelar);

    /** Transições PERMITIDAS. Tudo que não está aqui deve falhar. */
    private static final Map<StatusBoleto, Map<String, StatusBoleto>> PERMITIDAS = Map.of(
            EM_EDICAO, Map.of("emitir", EMITIDO),
            EMITIDO, Map.of("notificar", NOTIFICADO, "cancelar", CANCELADO,
                    "pagamentoConforme", PAGO, "pagamentoDivergente", EM_ANALISE),
            NOTIFICADO, Map.of("notificar", NOTIFICADO, "cancelar", CANCELADO,
                    "pagamentoConforme", PAGO, "pagamentoDivergente", EM_ANALISE),
            EM_ANALISE, Map.of("aceitarPagamento", PAGO, "devolverPagamento", DEVOLVIDO),
            // cancelado não aceita ações do usuário, mas dinheiro recebido mesmo assim vai para análise
            CANCELADO, Map.of("pagamentoConforme", EM_ANALISE, "pagamentoDivergente", EM_ANALISE),
            PAGO, Map.of(),
            DEVOLVIDO, Map.of());

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
    void somenteEmEdicaoPermiteEditarEExcluir() {
        for (var status : StatusBoleto.values()) {
            var estado = EstadoBoleto.de(status);
            if (status == EM_EDICAO) {
                estado.validarEdicao();
                estado.validarExclusao();
            } else {
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarEdicao);
                assertThrows(TransicaoEstadoInvalidaException.class, estado::validarExclusao);
            }
        }
    }

    @Test
    void emAbertoSoDepoisDeEmitido() {
        var emAberto = Set.of(EMITIDO, NOTIFICADO); // rascunho não é cobrável nem conta no limite
        for (var status : StatusBoleto.values()) {
            assertEquals(emAberto.contains(status), EstadoBoleto.de(status).emAberto(), status.name());
        }
    }
}
