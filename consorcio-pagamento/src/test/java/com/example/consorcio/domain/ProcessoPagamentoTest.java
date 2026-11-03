package com.example.consorcio.domain;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testes do domínio: Java puro, sem Quarkus, sem banco. Rodam em milissegundos. */
class ProcessoPagamentoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    private static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    private static final IdentificadorCota COTA = new IdentificadorCota(1234, 10);

    private ProcessoPagamento abrir(Dinheiro valor, FormaPagamento forma, ContaDebito conta, LocalDate vencimento) {
        return ProcessoPagamento.abrir(COTA, valor, forma, conta, vencimento, AGORA, HOJE, 30);
    }

    @Test
    void abreProcessoValidoComStatusAberto() {
        var processo = abrir(Dinheiro.de("500.00"), FormaPagamento.PIX, null, HOJE.plusDays(5));

        assertEquals(StatusProcessoPagamento.ABERTO, processo.getStatus());
        assertEquals(Dinheiro.de("500"), processo.getValor());
    }

    @Test
    void rejeitaValorZero() {
        var e = assertThrows(RegraNegocioException.class,
                () -> abrir(Dinheiro.ZERO, FormaPagamento.PIX, null, HOJE));
        assertEquals("VALOR_NAO_POSITIVO", e.getCodigo());
    }

    @Test
    void rejeitaValorComMaisDeDuasCasas() {
        var e = assertThrows(RegraNegocioException.class, () -> Dinheiro.de("10.001"));
        assertEquals("VALOR_CASAS_DECIMAIS", e.getCodigo());
    }

    @Test
    void rejeitaVencimentoNoPassado() {
        var e = assertThrows(RegraNegocioException.class,
                () -> abrir(Dinheiro.de("100"), FormaPagamento.BOLETO, null, HOJE.minusDays(1)));
        assertEquals("VENCIMENTO_NO_PASSADO", e.getCodigo());
    }

    @Test
    void rejeitaVencimentoAlemDoPrazo() {
        var e = assertThrows(RegraNegocioException.class,
                () -> abrir(Dinheiro.de("100"), FormaPagamento.BOLETO, null, HOJE.plusDays(31)));
        assertEquals("VENCIMENTO_ALEM_DO_PRAZO", e.getCodigo());
    }

    @Test
    void debitoEmContaExigeConta() {
        var e = assertThrows(RegraNegocioException.class,
                () -> abrir(Dinheiro.de("100"), FormaPagamento.DEBITO_EM_CONTA, null, HOJE));
        assertEquals("CONTA_DEBITO_OBRIGATORIA", e.getCodigo());
    }

    @Test
    void contaSoPodeSerInformadaParaDebito() {
        var e = assertThrows(RegraNegocioException.class,
                () -> abrir(Dinheiro.de("100"), FormaPagamento.PIX, new ContaDebito("1234", "56789"), HOJE));
        assertEquals("CONTA_DEBITO_INDEVIDA", e.getCodigo());
    }

    @Test
    void naoCancelaProcessoJaConfirmado() {
        var processo = abrir(Dinheiro.de("100"), FormaPagamento.PIX, null, HOJE);
        processo.confirmar();

        var e = assertThrows(RegraNegocioException.class, () -> processo.cancelar("desistência"));
        assertEquals("TRANSICAO_INVALIDA", e.getCodigo());
    }

    @Test
    void cancelamentoExigeMotivo() {
        var processo = abrir(Dinheiro.de("100"), FormaPagamento.PIX, null, HOJE);

        assertThrows(RegraNegocioException.class, () -> processo.cancelar(" "));
        processo.cancelar("cliente desistiu");
        assertEquals(StatusProcessoPagamento.CANCELADO, processo.getStatus());
    }
}
