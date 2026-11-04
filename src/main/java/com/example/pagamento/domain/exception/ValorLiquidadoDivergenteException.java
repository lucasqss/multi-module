package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.Dinheiro;

/** Conformidade do retorno: valor liquidado diferente do total (vira motivo de análise, não erro). */
public class ValorLiquidadoDivergenteException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_LIQUIDADO_DIVERGENTE";

    public ValorLiquidadoDivergenteException(Dinheiro liquidado, Dinheiro esperado) {
        super(CODIGO, "Banco liquidou %s, mas o processo soma %s".formatted(liquidado, esperado));
    }
}
