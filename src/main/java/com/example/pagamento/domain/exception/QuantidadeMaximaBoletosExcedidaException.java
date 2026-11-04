package com.example.pagamento.domain.exception;

/** Seção de boletos com boletos demais. */
public class QuantidadeMaximaBoletosExcedidaException extends RegraNegocioException {

    public static final String CODIGO = "QUANTIDADE_MAXIMA_BOLETOS_EXCEDIDA";

    public QuantidadeMaximaBoletosExcedidaException(int maximo) {
        super(CODIGO, "Um processo aceita no máximo %d boletos".formatted(maximo));
    }
}
