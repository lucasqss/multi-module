package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.function.Function;

import static com.example.pagamento.domain.state.StatusPagamento.AGENDADO;
import static com.example.pagamento.domain.state.StatusPagamento.CANCELADO;
import static com.example.pagamento.domain.state.StatusPagamento.EM_ANALISE;
import static com.example.pagamento.domain.state.StatusPagamento.EM_EDICAO;
import static com.example.pagamento.domain.state.StatusPagamento.EM_PROCESSAMENTO;
import static com.example.pagamento.domain.state.StatusPagamento.PAGO;
import static com.example.pagamento.domain.state.StatusPagamento.RECUSADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A máquina de estados (enum) como TABELA: para cada ação, de onde se pode sair e para onde se vai. */
class StatusPagamentoTest {

    private static final Map<String, Function<StatusPagamento, StatusPagamento>> ACOES = Map.of(
            "submeter", StatusPagamento::submeter,
            "enviar", StatusPagamento::enviarAoBanco,
            "reenviar", StatusPagamento::reenviarAoBanco,
            "cancelar", StatusPagamento::cancelar);

    /** ação -> (origem -> destino). O que não está aqui deve dar 409. */
    private static final Map<String, Map<StatusPagamento, StatusPagamento>> PERMITIDAS = Map.of(
            "submeter", Map.of(EM_EDICAO, AGENDADO),
            "enviar", Map.of(AGENDADO, EM_PROCESSAMENTO),
            "reenviar", Map.of(RECUSADO, EM_PROCESSAMENTO),
            "cancelar", Map.of(EM_EDICAO, CANCELADO, AGENDADO, CANCELADO, RECUSADO, CANCELADO, EM_ANALISE, CANCELADO));

    @ParameterizedTest
    @EnumSource(StatusPagamento.class)
    void cadaEstadoAceitaSoAsAcoesDaTabela(StatusPagamento origem) {
        ACOES.forEach((acao, executar) -> {
            var destino = PERMITIDAS.get(acao).get(origem);
            if (destino == null) {
                assertThrows(TransicaoEstadoInvalidaException.class, () -> executar.apply(origem), acao + " em " + origem);
            } else {
                assertEquals(destino, executar.apply(origem), acao + " em " + origem);
            }
        });
    }

    @Test
    void retornoDoBancoSoEmProcessamento() {
        assertEquals(PAGO, EM_PROCESSAMENTO.receberRetornoBanco(true, true));
        assertEquals(EM_ANALISE, EM_PROCESSAMENTO.receberRetornoBanco(true, false));
        assertEquals(RECUSADO, EM_PROCESSAMENTO.receberRetornoBanco(false, true));

        for (var outro : StatusPagamento.values()) {
            if (outro != EM_PROCESSAMENTO) {
                assertThrows(TransicaoEstadoInvalidaException.class,
                        () -> outro.receberRetornoBanco(true, true));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(StatusPagamento.class)
    void soORascunhoEditaEExclui(StatusPagamento status) {
        if (status == EM_EDICAO) {
            status.validarEdicao();
            status.validarExclusao();
        } else {
            assertThrows(TransicaoEstadoInvalidaException.class, status::validarEdicao);
            assertThrows(TransicaoEstadoInvalidaException.class, status::validarExclusao);
        }
    }
}
