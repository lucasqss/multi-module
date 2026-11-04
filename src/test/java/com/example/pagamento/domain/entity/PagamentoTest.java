package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.Cenarios;
import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;
import com.example.pagamento.domain.exception.ValorLiquidadoDivergenteException;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.RetornoBanco;
import org.junit.jupiter.api.Test;

import static com.example.pagamento.domain.Cenarios.HOJE;
import static com.example.pagamento.domain.Cenarios.liquidado;
import static com.example.pagamento.domain.Cenarios.recusado;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O agregado: ordem estado (409) -> regras (422) -> mudança, e o tratamento do fato externo. */
class PagamentoTest {

    @Test
    void rascunhoPodeFicarIncompletoMasNaoSubmeteInvalido() {
        var p = Cenarios.individual().dataPagamento(HOJE.minusDays(1)).rascunho(); // aceito na edição

        assertThrows(DataPagamentoNoPassadoException.class, () -> p.submeter(HOJE));
        assertEquals(StatusPagamento.EM_EDICAO, p.getStatus()); // regra violada: nada mudou
    }

    @Test
    void estadoBarraAntesDasRegras() {
        var p = Cenarios.individual().agendado();
        assertThrows(TransicaoEstadoInvalidaException.class, () -> p.submeter(HOJE));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> p.salvarBoletos(Cenarios.individual().boletos()));
    }

    @Test
    void envioGeraTransacaoEPagoQuandoLiquidaOTotal() {
        var p = Cenarios.processo().enviado();
        assertEquals(p.getId() + "-1", p.getIdTransacao().orElseThrow());

        p.registrarRetornoBanco(liquidado(p, "500.00"));

        assertEquals(StatusPagamento.PAGO, p.getStatus());
        assertEquals("AUT-1", p.getAutenticacao().orElseThrow());
    }

    @Test
    void retornoRepetidoEIgnorado() {
        var p = Cenarios.individual().enviado();
        var retorno = liquidado(p, "500.00");
        p.registrarRetornoBanco(retorno);
        p.registrarRetornoBanco(retorno); // o mesmo aviso de novo: não lança, nada muda (idempotente)
        assertEquals(StatusPagamento.PAGO, p.getStatus());
    }

    @Test
    void valorDivergenteNaoERecusadoVaiParaAnalise() {
        var p = Cenarios.lote().enviado();

        p.registrarRetornoBanco(liquidado(p, "499.99"));

        assertEquals(StatusPagamento.EM_ANALISE, p.getStatus());
        assertEquals(ValorLiquidadoDivergenteException.CODIGO, p.getMotivoAnalise().orElseThrow().codigo());
    }

    @Test
    void recusadoPodeSerReenviadoComNovaTransacao() {
        var p = Cenarios.individual().enviado();
        p.registrarRetornoBanco(recusado(p));
        assertEquals(StatusPagamento.RECUSADO, p.getStatus());

        var antiga = p.getIdTransacao().orElseThrow();
        p.reenviarAoBanco();

        assertEquals(StatusPagamento.EM_PROCESSAMENTO, p.getStatus());
        assertEquals(p.getId() + "-2", p.getIdTransacao().orElseThrow());
        assertTrue(p.getMotivoRecusa().isEmpty());
        p.registrarRetornoBanco(new RetornoBanco(
                antiga, false, null, null, "atrasado", Cenarios.AGORA)); // retorno da transação antiga: ignorado
        assertEquals(StatusPagamento.EM_PROCESSAMENTO, p.getStatus());
    }

    @Test
    void cancelarGuardaOMotivo() {
        var p = Cenarios.individual().agendado();
        p.cancelar(new Motivo("fornecedor pediu"));
        assertEquals(StatusPagamento.CANCELADO, p.getStatus());
        assertEquals("fornecedor pediu", p.getMotivoCancelamento().orElseThrow().texto());
    }
}
