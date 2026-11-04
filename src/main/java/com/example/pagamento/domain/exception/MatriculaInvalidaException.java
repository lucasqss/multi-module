package com.example.pagamento.domain.exception;

/** Matrícula de solicitante/aprovador fora do formato. */
public class MatriculaInvalidaException extends DadoInvalidoException {

    public static final String CODIGO = "MATRICULA_INVALIDA";

    public MatriculaInvalidaException(String valor) {
        super(CODIGO, "Matrícula inválida: " + valor);
    }
}
