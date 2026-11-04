package com.example.financiamento.domain.exception;

/** O órgão registrou a garantia com outra identificação. É um FATO: não recusa, manda para pendência. */
public class IdentificacaoRegistradaDivergenteException extends RegraNegocioException {

    public static final String CODIGO = "IDENTIFICACAO_REGISTRADA_DIVERGENTE";

    public IdentificacaoRegistradaDivergenteException(String registrada, String esperada) {
        super(CODIGO, "Garantia registrada como %s, mas o contrato é do bem %s".formatted(registrada, esperada));
    }
}
