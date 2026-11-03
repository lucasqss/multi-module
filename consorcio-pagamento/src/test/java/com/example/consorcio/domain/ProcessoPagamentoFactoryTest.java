package com.example.consorcio.domain;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.factory.ProcessoPagamentoFactory;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessoPagamentoFactoryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);
    private final ProcessoPagamentoFactory factory = new ProcessoPagamentoFactory(clock, 30);
    private final LocalDate vencimento = LocalDate.of(2026, 11, 10);

    private Cota cota(SituacaoCota situacao, String saldo) {
        return new Cota(new IdentificadorCota(1, 1), situacao, Dinheiro.de(saldo));
    }

    @Test
    void cotaInadimplentePodePagar() {
        assertDoesNotThrow(() -> factory.criar(cota(SituacaoCota.INADIMPLENTE, "1000"),
                Dinheiro.de("100"), FormaPagamento.BOLETO, null, vencimento));
    }

    @Test
    void cotaCanceladaNaoAceitaPagamento() {
        var e = assertThrows(RegraNegocioException.class, () -> factory.criar(cota(SituacaoCota.CANCELADA, "1000"),
                Dinheiro.de("100"), FormaPagamento.BOLETO, null, vencimento));
        assertEquals("COTA_NAO_ACEITA_PAGAMENTO", e.getCodigo());
    }

    @Test
    void valorNaoPodeExcederSaldoDevedor() {
        var e = assertThrows(RegraNegocioException.class, () -> factory.criar(cota(SituacaoCota.ATIVA, "1000"),
                Dinheiro.de("1000.01"), FormaPagamento.BOLETO, null, vencimento));
        assertEquals("VALOR_EXCEDE_SALDO_DEVEDOR", e.getCodigo());
    }
}
