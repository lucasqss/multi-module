package com.example.pagamento.domain.exception;

/** Segregação de funções: solicitante não aprova. */
public class AprovadorIgualAoSolicitanteException extends RegraNegocioException {

    public static final String CODIGO = "APROVADOR_IGUAL_AO_SOLICITANTE";

    public AprovadorIgualAoSolicitanteException() {
        super(CODIGO, "Quem solicitou o pagamento não pode aprová-lo");
    }
}
