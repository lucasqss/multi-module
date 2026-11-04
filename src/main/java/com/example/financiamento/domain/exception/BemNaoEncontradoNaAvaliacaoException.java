package com.example.financiamento.domain.exception;

/** O sistema de avaliação não conhece o bem informado (matrícula ou placa). */
public class BemNaoEncontradoNaAvaliacaoException extends RegraNegocioException {

    public static final String CODIGO = "BEM_NAO_ENCONTRADO_NA_AVALIACAO";

    public BemNaoEncontradoNaAvaliacaoException(String identificacao) {
        super(CODIGO, "O bem %s não foi encontrado no sistema de avaliação".formatted(identificacao));
    }
}
