package com.example.financiamento.domain.exception;

/** Submissão sem valor solicitado ou prazo. */
public class CondicoesNaoInformadasException extends RegraNegocioException {

    public static final String CODIGO = "CONDICOES_NAO_INFORMADAS";

    public CondicoesNaoInformadasException() {
        super(CODIGO, "Informe o valor solicitado e o prazo");
    }
}
