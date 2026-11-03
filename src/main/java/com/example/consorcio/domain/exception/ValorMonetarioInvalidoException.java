package com.example.consorcio.domain.exception;

/** Valor negativo ou com mais de 2 casas decimais. */
public class ValorMonetarioInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "VALOR_MONETARIO_INVALIDO";

    public ValorMonetarioInvalidoException(String motivo) {
        super(CODIGO, "Valor monetário inválido: " + motivo);
    }
}
