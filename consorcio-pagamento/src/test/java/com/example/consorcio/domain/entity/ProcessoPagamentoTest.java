package com.example.consorcio.domain.entity;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.exception.TransicaoEstadoInvalidaException;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.Motivo;
import org.junit.jupiter.api.Test;

import static com.example.consorcio.domain.Cenarios.solicitacao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Garante que a entidade APLICA specifications e state — não testa as regras em si (já testadas). */
class ProcessoPagamentoTest {

    @Test
    void abreComStatusAberto() {
        var processo = solicitacao().abrir();
        assertEquals(StatusProcessoPagamento.ABERTO, processo.getStatus());
        assertTrue(processo.emAndamento());
    }

    @Test
    void naoAbreComSolicitacaoInvalida() {
        assertThrows(RegraNegocioException.class, () -> solicitacao().valor("0").forma(FormaPagamento.PIX).abrir());
    }

    @Test
    void alteraQuandoAbertoEValido() {
        var processo = solicitacao().abrir();
        processo.alterar(solicitacao().valor("750.00").build());
        assertEquals(Dinheiro.de("750"), processo.getValor());
    }

    @Test
    void alteracaoInvalidaNaoMudaNada() {
        var processo = solicitacao().abrir();
        assertThrows(RegraNegocioException.class, () -> processo.alterar(solicitacao().valor("9.99").build()));
        assertEquals(Dinheiro.de("500"), processo.getValor());
    }

    @Test
    void estadoBloqueiaAlteracaoAntesDasRegras() {
        var processo = solicitacao().abrir();
        processo.enviarParaProcessamento();
        // mesmo com dados inválidos, o erro é de ESTADO (409), não de regra (422)
        assertThrows(TransicaoEstadoInvalidaException.class,
                () -> processo.alterar(solicitacao().valor("0").build()));
    }

    @Test
    void cicloRejeitarReabrirReenviarConfirmar() {
        var processo = solicitacao().abrir();
        processo.enviarParaProcessamento();
        processo.rejeitar(new Motivo("conta sem saldo"));
        assertEquals("conta sem saldo", processo.getMotivoRejeicao().orElseThrow().texto());

        processo.reabrir();
        assertTrue(processo.getMotivoRejeicao().isEmpty());

        processo.enviarParaProcessamento();
        processo.confirmar();
        assertEquals(StatusProcessoPagamento.CONFIRMADO, processo.getStatus());
        assertTrue(processo.finalizado());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> processo.cancelar(new Motivo("tarde demais")));
    }

    @Test
    void motivoEhObrigatorio() {
        assertThrows(RegraNegocioException.class, () -> new Motivo("  "));
    }
}
