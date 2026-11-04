package com.example.pagamento.domain.state;

import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.Set;
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

/** A máquina de estados como TABELA: para cada ação, de onde se pode sair e para onde se vai. */
class EstadoPagamentoTest {

    private static final Map<String, Function<EstadoPagamento, EstadoPagamento>> ACOES = Map.of(
            "submeter", EstadoPagamento::submeter,
            "enviar", EstadoPagamento::enviarAoBanco,
            "reenviar", EstadoPagamento::reenviarAoBanco,
            "cancelar", EstadoPagamento::cancelar);

    /** ação -> (origem -> destino). O que não está aqui deve dar 409. */
    private static final Map<String, Map<StatusPagamento, StatusPagamento>> PERMITIDAS = Map.of(
            "submeter", Map.of(EM_EDICAO, AGENDADO),
            "enviar", Map.of(AGENDADO, EM_PROCESSAMENTO),
            "reenviar", Map.of(RECUSADO, EM_PROCESSAMENTO),
            "cancelar", Map.of(EM_EDICAO, CANCELADO, AGENDADO, CANCELADO, RECUSADO, CANCELADO, EM_ANALISE, CANCELADO));

    @ParameterizedTest
    @EnumSource(StatusPagamento.class)
    void cadaEstadoAceitaSoAsAcoesDaTabela(StatusPagamento origem) {
        var estado = EstadoPagamento.de(origem);
        assertEquals(origem, estado.status());
        ACOES.forEach((acao, executar) -> {
            var destino = PERMITIDAS.get(acao).get(origem);
            if (destino == null) {
                assertThrows(TransicaoEstadoInvalidaException.class, () -> executar.apply(estado), acao + " em " + origem);
            } else {
                assertEquals(destino, executar.apply(estado).status(), acao + " em " + origem);
            }
        });
    }

    @Test
    void retornoDoBancoSoEmProcessamento() {
        var emProcessamento = EstadoPagamento.de(EM_PROCESSAMENTO);
        assertEquals(PAGO, emProcessamento.receberRetornoBanco(true, true).status());
        assertEquals(EM_ANALISE, emProcessamento.receberRetornoBanco(true, false).status());
        assertEquals(RECUSADO, emProcessamento.receberRetornoBanco(false, true).status());

        for (var outro : StatusPagamento.values()) {
            if (outro != EM_PROCESSAMENTO) {
                assertThrows(TransicaoEstadoInvalidaException.class,
                        () -> EstadoPagamento.de(outro).receberRetornoBanco(true, true));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(StatusPagamento.class)
    void soORascunhoEditaEExclui(StatusPagamento status) {
        var estado = EstadoPagamento.de(status);
        if (Set.of(EM_EDICAO).contains(status)) {
            estado.validarEdicao();
            estado.validarExclusao();
        } else {
            assertThrows(TransicaoEstadoInvalidaException.class, estado::validarEdicao);
            assertThrows(TransicaoEstadoInvalidaException.class, estado::validarExclusao);
        }
    }
}
