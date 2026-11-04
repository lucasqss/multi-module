package com.example.pagamento.application.pagamento.dto;

import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.RetornoBanco;

import java.math.BigDecimal;
import java.time.Instant;

/** Retorno do banco, já traduzido do formato dele pelo entrypoint. */
public record RetornoBancoInput(String idTransacao, boolean liquidado, BigDecimal valorLiquidado,
                                String codigoAutenticacao, String motivoRecusa, Instant momento) {

    public RetornoBanco retorno() {
        return new RetornoBanco(idTransacao, liquidado, valorLiquidado == null ? null : new Dinheiro(valorLiquidado),
                codigoAutenticacao, motivoRecusa, momento);
    }
}
