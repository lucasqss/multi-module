package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.Matricula;

/** A mesma pessoa não aprova duas vezes. */
public class AprovadorRepetidoException extends RegraNegocioException {

    public static final String CODIGO = "APROVADOR_REPETIDO";

    public AprovadorRepetidoException(Matricula aprovador) {
        super(CODIGO, "%s já aprovou este processo".formatted(aprovador));
    }
}
