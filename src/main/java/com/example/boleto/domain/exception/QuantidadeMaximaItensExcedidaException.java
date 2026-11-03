package com.example.boleto.domain.exception;

/** Seção de itens com itens demais. */
public class QuantidadeMaximaItensExcedidaException extends RegraNegocioException {

    public static final String CODIGO = "QUANTIDADE_MAXIMA_ITENS_EXCEDIDA";

    public QuantidadeMaximaItensExcedidaException(int maximo) {
        super(CODIGO, "O boleto aceita no máximo %d itens".formatted(maximo));
    }
}
