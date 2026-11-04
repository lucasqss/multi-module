package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.valueobject.TipoFinanciamento;

/** Identificação do bem fora do formato do tipo (matrícula: só dígitos; placa: padrão Mercosul). */
public class IdentificacaoDoBemInvalidaException extends DadoInvalidoException {

    public static final String CODIGO = "IDENTIFICACAO_DO_BEM_INVALIDA";

    public IdentificacaoDoBemInvalidaException(TipoFinanciamento tipo, String identificacao) {
        super(CODIGO, "Identificação inválida para %s: %s".formatted(tipo, identificacao));
    }
}
