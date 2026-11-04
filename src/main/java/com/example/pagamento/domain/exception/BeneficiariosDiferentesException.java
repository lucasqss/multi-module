package com.example.pagamento.domain.exception;

/** Modalidade PROCESSO: todos os boletos precisam ser do mesmo beneficiário. */
public class BeneficiariosDiferentesException extends RegraNegocioException {

    public static final String CODIGO = "BENEFICIARIOS_DIFERENTES";

    public BeneficiariosDiferentesException() {
        super(CODIGO, "Um pagamento PROCESSO só aceita boletos do mesmo beneficiário (use LOTE para vários)");
    }
}
