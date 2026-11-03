package com.example.boleto.domain.entity;

import com.example.boleto.domain.enums.StatusBoleto;
import com.example.boleto.domain.exception.MotivoInvalidoException;
import com.example.boleto.domain.exception.TransicaoEstadoInvalidaException;
import com.example.boleto.domain.exception.ValorAbaixoDoMinimoException;
import com.example.boleto.domain.exception.ValorPagoDivergenteException;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagamento;
import org.junit.jupiter.api.Test;

import static com.example.boleto.domain.Cenarios.AGORA;
import static com.example.boleto.domain.Cenarios.HOJE;
import static com.example.boleto.domain.Cenarios.solicitacao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Garante que a entidade APLICA specifications e state; as regras em si já são testadas à parte. */
class BoletoTest {

    private static Pagamento pagamentoDe(String valor) {
        return new Pagamento(Dinheiro.de(valor), HOJE);
    }

    @Test
    void emiteComLinhaDigitavelEStatusEmitido() {
        var boleto = solicitacao().emitir();
        assertEquals(StatusBoleto.EMITIDO, boleto.getStatus());
        assertEquals(47, boleto.getLinhaDigitavel().valor().length());
        assertTrue(boleto.getLinhaDigitavel().valor().endsWith("0000050000")); // R$ 500,00 em centavos
        assertTrue(boleto.emAberto());
    }

    @Test
    void naoEmiteComSolicitacaoInvalida() {
        assertThrows(ValorAbaixoDoMinimoException.class, () -> solicitacao().valor("1.00").emitir());
    }

    @Test
    void alterarRegeraALinhaDigitavel() {
        var boleto = solicitacao().emitir();
        var linhaOriginal = boleto.getLinhaDigitavel();

        boleto.alterar(solicitacao().valor("750.00").build());

        assertEquals(Dinheiro.de("750"), boleto.getValor());
        assertNotEquals(linhaOriginal, boleto.getLinhaDigitavel());
    }

    @Test
    void estadoBloqueiaAlteracaoAntesDasRegras() {
        var boleto = solicitacao().emitir();
        boleto.registrarNotificacao(AGORA);
        // mesmo com dados inválidos, o erro é de ESTADO (409), não de regra (422)
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.alterar(solicitacao().valor("1.00").build()));
    }

    @Test
    void cicloEmitirNotificarReenviarPagar() {
        var boleto = solicitacao().emitir();
        boleto.registrarNotificacao(AGORA);
        boleto.registrarNotificacao(AGORA.plusSeconds(60)); // reenvio
        assertEquals(StatusBoleto.NOTIFICADO, boleto.getStatus());
        assertEquals(AGORA.plusSeconds(60), boleto.getNotificadoEm().orElseThrow());

        boleto.receberPagamento(pagamentoDe("500.00"), HOJE);

        assertEquals(StatusBoleto.PAGO, boleto.getStatus());
        assertEquals(Dinheiro.de("500"), boleto.getPagamento().orElseThrow().valorPago());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.cancelar(new Motivo("tarde demais")));
    }

    @Test
    void pagamentoInvalidoNaoMudaOEstado() {
        var boleto = solicitacao().emitir();
        assertThrows(ValorPagoDivergenteException.class, () -> boleto.receberPagamento(pagamentoDe("100.00"), HOJE));
        assertEquals(StatusBoleto.EMITIDO, boleto.getStatus());
        assertTrue(boleto.getPagamento().isEmpty());
    }

    @Test
    void boletoCanceladoNaoRecebePagamento() {
        var boleto = solicitacao().emitir();
        boleto.cancelar(new Motivo("emitido por engano"));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.receberPagamento(pagamentoDe("500.00"), HOJE));
    }

    @Test
    void motivoEhObrigatorio() {
        assertThrows(MotivoInvalidoException.class, () -> new Motivo("  "));
    }
}
