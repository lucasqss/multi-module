package com.example.financiamento.domain.exception;

/** CPF fora do formato (11 dígitos, não todos iguais). */
public class CpfInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "CPF_INVALIDO";

    public CpfInvalidoException() {
        super(CODIGO, "CPF inválido");
    }
}
