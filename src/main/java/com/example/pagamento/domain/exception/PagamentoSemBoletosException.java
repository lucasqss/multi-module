package com.example.pagamento.domain.exception;

/** Completude: submissão sem boletos. */
public class PagamentoSemBoletosException extends RegraNegocioException {

    public static final String CODIGO = "PAGAMENTO_SEM_BOLETOS";

    public PagamentoSemBoletosException() {
        super(CODIGO, "O pagamento precisa ter ao menos um boleto");
    }
}
