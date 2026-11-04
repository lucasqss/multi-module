package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.example.pagamento.domain.state.StatusProcesso.AGUARDANDO_APROVACAO;
import static com.example.pagamento.domain.state.StatusProcesso.APROVADO;
import static com.example.pagamento.domain.state.StatusProcesso.CANCELADO;
import static com.example.pagamento.domain.state.StatusProcesso.EM_ANALISE;
import static com.example.pagamento.domain.state.StatusProcesso.EM_EDICAO;
import static com.example.pagamento.domain.state.StatusProcesso.EM_PROCESSAMENTO;
import static com.example.pagamento.domain.state.StatusProcesso.FALHOU;
import static com.example.pagamento.domain.state.StatusProcesso.PAGO;
import static com.example.pagamento.domain.state.StatusProcesso.REJEITADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A máquina de estados inteira como TABELA: para cada (estado, ação), o destino esperado ou erro.
 * Se alguém mudar uma transição sem querer, este teste acusa.
 */
class EstadoProcessoTest {

    private static final Map<String, Function<EstadoProcesso, EstadoProcesso>> ACOES = new LinkedHashMap<>();

    static {
        ACOES.put("submeter", EstadoProcesso::submeter);
        ACOES.put("aprovarSemAlcada", e -> e.registrarAprovacao(false));
        ACOES.put("aprovarComAlcada", e -> e.registrarAprovacao(true));
        ACOES.put("rejeitar", EstadoProcesso::rejeitar);
        ACOES.put("reabrir", EstadoProcesso::reabrir);
        ACOES.put("enviar", EstadoProcesso::enviarParaPagamento);
        ACOES.put("retentar", EstadoProcesso::retentarPagamento);
        ACOES.put("retornoLiquidadoConforme", e -> e.receberRetornoBanco(true, true));
        ACOES.put("retornoLiquidadoDivergente", e -> e.receberRetornoBanco(true, false));
        ACOES.put("retornoRecusado", e -> e.receberRetornoBanco(false, true));
        ACOES.put("boletoCanceladoNoEmissor", EstadoProcesso::boletoCanceladoNoEmissor);
        ACOES.put("cancelar", EstadoProcesso::cancelar);
    }

    /** Transições PERMITIDAS. Tudo que não está aqui deve falhar (409). */
    private static final Map<StatusProcesso, Map<String, StatusProcesso>> PERMITIDAS = Map.of(
            EM_EDICAO, Map.of("submeter", AGUARDANDO_APROVACAO, "cancelar", CANCELADO,
                    "boletoCanceladoNoEmissor", CANCELADO),
            AGUARDANDO_APROVACAO, Map.of("aprovarSemAlcada", AGUARDANDO_APROVACAO, "aprovarComAlcada", APROVADO,
                    "rejeitar", REJEITADO, "cancelar", CANCELADO, "boletoCanceladoNoEmissor", CANCELADO),
            REJEITADO, Map.of("reabrir", EM_EDICAO, "cancelar", CANCELADO, "boletoCanceladoNoEmissor", CANCELADO),
            APROVADO, Map.of("enviar", EM_PROCESSAMENTO, "cancelar", CANCELADO, "boletoCanceladoNoEmissor", CANCELADO),
            // já no banco: o usuário não cancela; o boleto cancelado no emissor vira análise
            EM_PROCESSAMENTO, Map.of("retornoLiquidadoConforme", PAGO, "retornoLiquidadoDivergente", EM_ANALISE,
                    "retornoRecusado", FALHOU, "boletoCanceladoNoEmissor", EM_ANALISE),
            FALHOU, Map.of("retentar", EM_PROCESSAMENTO, "cancelar", CANCELADO, "boletoCanceladoNoEmissor", CANCELADO),
            PAGO, Map.of("boletoCanceladoNoEmissor", EM_ANALISE),
            EM_ANALISE, Map.of("retornoLiquidadoConforme", EM_ANALISE, "retornoLiquidadoDivergente", EM_ANALISE,
                    "retornoRecusado", EM_ANALISE, "boletoCanceladoNoEmissor", EM_ANALISE, "cancelar", CANCELADO),
            CANCELADO, Map.of("boletoCanceladoNoEmissor", CANCELADO));

    @Test
    void tabelaDeTransicoes() {
        for (var status : StatusProcesso.values()) {
            var estado = EstadoProcesso.de(status);
            assertEquals(status, estado.status());
            ACOES.forEach((acao, executar) -> {
                var destino = PERMITIDAS.get(status).get(acao);
                if (destino != null) {
                    assertEquals(destino, executar.apply(estado).status(), status + " --" + acao + "-->");
                } else {
                    assertThrows(TransicaoEstadoInvalidaException.class, () -> executar.apply(estado),
                            status + " não deveria aceitar " + acao);
                }
            });
        }
    }

    @Test
    void somenteEmEdicaoPermiteEditarEExcluir() {
        for (var status : StatusProcesso.values()) {
            var estado = EstadoProcesso.de(status);
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
    void somenteEmAnaliseExigeDecisaoHumana() {
        for (var status : StatusProcesso.values()) {
            assertEquals(status == EM_ANALISE, EstadoProcesso.de(status).emAnalise(), status.name());
        }
    }
}
