package com.example.pagamento.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado de um envio, informado pelo BANCO (sistema externo). É um FATO.
 * {@code liquidado = true}: o dinheiro saiu; {@code false}: o banco recusou (saldo, conta, boleto...).
 */
public record RetornoBanco(IdTransacao idTransacao, boolean liquidado, Dinheiro valorLiquidado,
                           String codigoAutenticacao, String motivoRecusa, Instant momento) {

    public RetornoBanco {
        Objects.requireNonNull(idTransacao, "id da transação é obrigatório");
        Objects.requireNonNull(momento);
        if (liquidado) {
            Objects.requireNonNull(valorLiquidado, "valor liquidado é obrigatório quando liquidado");
        }
    }
}
