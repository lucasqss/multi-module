package com.example.pagamento.domain.exception;

/** Completude: submissão sem boletos. */
public class ProcessoSemBoletosException extends RegraNegocioException {

    public static final String CODIGO = "PROCESSO_SEM_BOLETOS";

    public ProcessoSemBoletosException() {
        super(CODIGO, "O processo precisa ter ao menos um boleto");
    }
}
