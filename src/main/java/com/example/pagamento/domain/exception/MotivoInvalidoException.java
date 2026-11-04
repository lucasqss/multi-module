package com.example.pagamento.domain.exception;

/** Motivo de rejeição/cancelamento vazio ou longo demais. */
public class MotivoInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "MOTIVO_INVALIDO";

    public MotivoInvalidoException(String motivo) {
        super(CODIGO, "Motivo inválido: " + motivo);
    }
}
