package com.example.boleto.domain.entity;

import com.example.boleto.domain.exception.BoletoNaoEstaEmAbertoException;
import com.example.boleto.domain.exception.BoletoSemItensException;
import com.example.boleto.domain.exception.MotivoInvalidoException;
import com.example.boleto.domain.exception.QuantidadeMaximaItensExcedidaException;
import com.example.boleto.domain.exception.TransicaoEstadoInvalidaException;
import com.example.boleto.domain.exception.ValorPagoDivergenteException;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Email;
import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagamento;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.example.boleto.domain.Cenarios.AGORA;
import static com.example.boleto.domain.Cenarios.HOJE;
import static com.example.boleto.domain.Cenarios.boleto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Garante que a entidade APLICA specifications e state; as regras em si já são testadas à parte. */
class BoletoTest {

    private static ItensBoleto itens(String... valores) {
        return new ItensBoleto(List.of(valores).stream().map(v -> new ItemBoleto("Item " + v, Dinheiro.de(v))).toList());
    }

    private static Pagamento pagamentoDe(String valor) {
        return pagamentoDe("TX-1", valor);
    }

    private static Pagamento pagamentoDe(String idTransacao, String valor) {
        return new Pagamento(idTransacao, Dinheiro.de(valor), HOJE);
    }

    @Test
    void nasceEmEdicaoIncompletoESemLinhaDigitavel() {
        var boleto = boleto().semItens().rascunho();
        assertEquals(StatusBoleto.EM_EDICAO, boleto.getStatus());
        assertTrue(boleto.getItens().vazio());
        assertTrue(boleto.getLinhaDigitavel().isEmpty());
        assertFalse(boleto.emAberto()); // rascunho não conta no limite de boletos em aberto
    }

    @Test
    void secoesSaoSalvasPorPartesEOValorEhASomaDosItens() {
        var boleto = boleto().semItens().rascunho();
        boleto.salvarItens(itens("300.00", "200.00"));
        boleto.salvarDestinatarios(new Destinatarios(List.of(new Email("financeiro@empresa.com"))));
        boleto.alterarVencimento(HOJE.plusDays(14));

        assertEquals(Dinheiro.de("500"), boleto.getValor());
        assertEquals(1, boleto.getDestinatarios().quantidade());
        assertEquals(StatusBoleto.EM_EDICAO, boleto.getStatus());
    }

    @Test
    void secaoInvalidaNaoMudaNada() {
        var boleto = boleto().rascunho();
        var muitos = Collections.nCopies(21, "10.00").toArray(String[]::new);
        assertThrows(QuantidadeMaximaItensExcedidaException.class, () -> boleto.salvarItens(itens(muitos)));
        assertEquals(Dinheiro.de("500"), boleto.getValor());
    }

    @Test
    void emissaoExigeCompletude() {
        var boleto = boleto().semItens().rascunho();
        assertThrows(BoletoSemItensException.class, () -> boleto.emitir(boleto().condicoes()));
        assertEquals(StatusBoleto.EM_EDICAO, boleto.getStatus()); // continua editável
    }

    @Test
    void emissaoGeraLinhaDigitavelComOValorTotal() {
        var boleto = boleto().emitido();
        assertEquals(StatusBoleto.EMITIDO, boleto.getStatus());
        assertTrue(boleto.getLinhaDigitavel().orElseThrow().valor().endsWith("0000050000")); // R$ 500,00
        assertEquals(AGORA, boleto.getEmitidoEm().orElseThrow());
        assertTrue(boleto.emAberto());
    }

    @Test
    void depoisDeEmitidoNaoEditaNemExclui_estadoAntesDasRegras() {
        var boleto = boleto().emitido();
        // mesmo com dados inválidos, o erro é de ESTADO (409), não de regra (422)
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.salvarItens(itens("0")));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.alterarVencimento(HOJE.plusDays(10)));
        assertThrows(TransicaoEstadoInvalidaException.class, boleto::validarExclusao);
    }

    @Test
    void rascunhoNaoRecebePagamento() {
        var boleto = boleto().rascunho();
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.registrarPagamento(pagamentoDe("500.00"), HOJE));
    }

    @Test
    void cicloEmitirNotificarReenviarPagar() {
        var boleto = boleto().emitido();
        boleto.registrarNotificacao(AGORA);
        boleto.registrarNotificacao(AGORA.plusSeconds(60)); // reenvio
        assertEquals(StatusBoleto.NOTIFICADO, boleto.getStatus());
        assertEquals(AGORA.plusSeconds(60), boleto.getNotificadoEm().orElseThrow());

        boleto.registrarPagamento(pagamentoDe("500.00"), HOJE);

        assertEquals(StatusBoleto.PAGO, boleto.getStatus());
        assertTrue(boleto.getMotivoAnalise().isEmpty());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto.cancelar(new Motivo("tarde demais")));
    }

    // ===================== pagamento recebido do sistema externo (FATO) =====================

    @Test
    void pagamentoDivergenteNaoEhRecusado_vaiParaAnaliseComOMotivo() {
        var boleto = boleto().emitido();

        boleto.registrarPagamento(pagamentoDe("100.00"), HOJE); // não lança: o dinheiro já entrou

        assertEquals(StatusBoleto.EM_ANALISE, boleto.getStatus());
        assertEquals(ValorPagoDivergenteException.CODIGO, boleto.getMotivoAnalise().orElseThrow().codigo());
        assertEquals(Dinheiro.de("100"), boleto.getPagamento().orElseThrow().valorPago());
    }

    @Test
    void avisoRepetidoDaMesmaTransacaoEhIgnorado() {
        var boleto = boleto().emitido();
        boleto.registrarPagamento(pagamentoDe("TX-9", "500.00"), HOJE);

        boleto.registrarPagamento(pagamentoDe("TX-9", "500.00"), HOJE); // webhook reenviado

        assertEquals(StatusBoleto.PAGO, boleto.getStatus());
    }

    @Test
    void outraTransacaoParaBoletoJaPagoEhRecusadaPeloEstado() {
        var boleto = boleto().emitido();
        boleto.registrarPagamento(pagamentoDe("TX-1", "500.00"), HOJE);
        assertThrows(TransicaoEstadoInvalidaException.class,
                () -> boleto.registrarPagamento(pagamentoDe("TX-2", "500.00"), HOJE));
    }

    @Test
    void pagamentoDeBoletoCanceladoVaiParaAnalise() {
        var boleto = boleto().emitido();
        boleto.cancelar(new Motivo("emitido por engano"));

        boleto.registrarPagamento(pagamentoDe("500.00"), HOJE);

        assertEquals(StatusBoleto.EM_ANALISE, boleto.getStatus());
        assertEquals(BoletoNaoEstaEmAbertoException.CODIGO, boleto.getMotivoAnalise().orElseThrow().codigo());
    }

    @Test
    void financeiroAceitaOuDevolve() {
        var aceito = boleto().emitido();
        aceito.registrarPagamento(pagamentoDe("499.00"), HOJE);
        aceito.aceitarPagamento();
        assertEquals(StatusBoleto.PAGO, aceito.getStatus());

        var devolvido = boleto().emitido();
        devolvido.registrarPagamento(pagamentoDe("499.00"), HOJE);
        devolvido.devolverPagamento(new Motivo("valor menor que o devido"));
        assertEquals(StatusBoleto.DEVOLVIDO, devolvido.getStatus());

        // sem análise pendente, não há o que aceitar
        assertThrows(TransicaoEstadoInvalidaException.class, () -> boleto().emitido().aceitarPagamento());
    }

    @Test
    void motivoEhObrigatorio() {
        assertThrows(MotivoInvalidoException.class, () -> new Motivo("  "));
    }
}
