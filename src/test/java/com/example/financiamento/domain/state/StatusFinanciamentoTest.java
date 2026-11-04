package com.example.financiamento.domain.state;

import com.example.financiamento.domain.exception.TransicaoEstadoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.function.Function;

import static com.example.financiamento.domain.state.StatusFinanciamento.APROVADO;
import static com.example.financiamento.domain.state.StatusFinanciamento.CANCELADO;
import static com.example.financiamento.domain.state.StatusFinanciamento.CONTRATADO;
import static com.example.financiamento.domain.state.StatusFinanciamento.EM_EDICAO;
import static com.example.financiamento.domain.state.StatusFinanciamento.EM_PENDENCIA;
import static com.example.financiamento.domain.state.StatusFinanciamento.EM_REGISTRO;
import static com.example.financiamento.domain.state.StatusFinanciamento.REGISTRO_RECUSADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A máquina de estados (enum) como TABELA: para cada ação, de onde se pode sair e para onde se vai. */
class StatusFinanciamentoTest {

    private static final Map<String, Function<StatusFinanciamento, StatusFinanciamento>> ACOES = Map.of(
            "submeter", StatusFinanciamento::submeter,
            "contratar", StatusFinanciamento::contratar,
            "reenviar", StatusFinanciamento::reenviarRegistro,
            "cancelar", StatusFinanciamento::cancelar);

    /** ação -> (origem -> destino). O que não está aqui deve dar 409. */
    private static final Map<String, Map<StatusFinanciamento, StatusFinanciamento>> PERMITIDAS = Map.of(
            "submeter", Map.of(EM_EDICAO, APROVADO),
            "contratar", Map.of(APROVADO, EM_REGISTRO),
            "reenviar", Map.of(REGISTRO_RECUSADO, EM_REGISTRO),
            "cancelar", Map.of(EM_EDICAO, CANCELADO, APROVADO, CANCELADO, REGISTRO_RECUSADO, CANCELADO, EM_PENDENCIA, CANCELADO));

    @ParameterizedTest
    @EnumSource(StatusFinanciamento.class)
    void cadaStatusAceitaSoAsAcoesDaTabela(StatusFinanciamento origem) {
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
    void retornoDoRegistroSoEmRegistro() {
        assertEquals(CONTRATADO, EM_REGISTRO.receberRetornoRegistro(true, true));
        assertEquals(EM_PENDENCIA, EM_REGISTRO.receberRetornoRegistro(true, false));
        assertEquals(REGISTRO_RECUSADO, EM_REGISTRO.receberRetornoRegistro(false, true));

        for (var outro : StatusFinanciamento.values()) {
            if (outro != EM_REGISTRO) {
                assertThrows(TransicaoEstadoInvalidaException.class, () -> outro.receberRetornoRegistro(true, true));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(StatusFinanciamento.class)
    void soORascunhoEditaEExclui(StatusFinanciamento status) {
        if (status == EM_EDICAO) {
            status.validarEdicao();
            status.validarExclusao();
        } else {
            assertThrows(TransicaoEstadoInvalidaException.class, status::validarEdicao);
            assertThrows(TransicaoEstadoInvalidaException.class, status::validarExclusao);
        }
    }
}
