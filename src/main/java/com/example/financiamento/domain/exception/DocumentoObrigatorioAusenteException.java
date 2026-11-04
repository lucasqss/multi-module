package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.valueobject.TipoDocumento;

/** Falta um documento exigido (os comuns ou os do tipo de financiamento). */
public class DocumentoObrigatorioAusenteException extends RegraNegocioException {

    public static final String CODIGO = "DOCUMENTO_OBRIGATORIO_AUSENTE";

    public DocumentoObrigatorioAusenteException(TipoDocumento faltante) {
        super(CODIGO, "Documento obrigatório ausente: " + faltante);
    }
}
