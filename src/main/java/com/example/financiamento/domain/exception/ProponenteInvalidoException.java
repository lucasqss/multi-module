package com.example.financiamento.domain.exception;

/** Dados do proponente inválidos (nome em branco, renda não positiva). */
public class ProponenteInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "PROPONENTE_INVALIDO";

    public ProponenteInvalidoException(String motivo) {
        super(CODIGO, "Proponente inválido: " + motivo);
    }
}
