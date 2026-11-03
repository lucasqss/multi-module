package com.example.boleto.domain.exception;

/** Seção de destinatários com e-mails demais. */
public class QuantidadeMaximaDestinatariosExcedidaException extends RegraNegocioException {

    public static final String CODIGO = "QUANTIDADE_MAXIMA_DESTINATARIOS_EXCEDIDA";

    public QuantidadeMaximaDestinatariosExcedidaException(int maximo) {
        super(CODIGO, "A notificação aceita no máximo %d destinatários extras".formatted(maximo));
    }
}
