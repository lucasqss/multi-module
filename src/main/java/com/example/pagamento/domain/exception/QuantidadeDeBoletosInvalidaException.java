package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.Modalidade;

/** A quantidade de boletos não cabe na modalidade (ex.: INDIVIDUAL com dois boletos). */
public class QuantidadeDeBoletosInvalidaException extends RegraNegocioException {

    public static final String CODIGO = "QUANTIDADE_DE_BOLETOS_INVALIDA";

    public QuantidadeDeBoletosInvalidaException(Modalidade modalidade, int minimo, int maximo) {
        super(CODIGO, "Pagamento %s aceita de %d a %d boletos".formatted(modalidade, minimo, maximo));
    }
}
