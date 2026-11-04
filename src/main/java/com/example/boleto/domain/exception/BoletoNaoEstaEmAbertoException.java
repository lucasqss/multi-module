package com.example.boleto.domain.exception;

import com.example.boleto.domain.state.StatusBoleto;

/** Pagamento recebido para um boleto que não está em aberto (ex.: cancelado). Motivo de análise. */
public class BoletoNaoEstaEmAbertoException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_NAO_ESTA_EM_ABERTO";

    public BoletoNaoEstaEmAbertoException(StatusBoleto status) {
        super(CODIGO, "Pagamento recebido para boleto com status " + status);
    }
}
