package com.example.consorcio.domain.exception;

/** Valor do pagamento igual a zero. */
public class ValorNaoPositivoException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_NAO_POSITIVO";

    public ValorNaoPositivoException() {
        super(CODIGO, "Valor do pagamento deve ser maior que zero");
    }
}
