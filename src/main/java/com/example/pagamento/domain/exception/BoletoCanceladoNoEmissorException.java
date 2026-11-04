package com.example.pagamento.domain.exception;

/** Fato do emissor: boleto cancelado com o pagamento já enviado (vira motivo de análise). */
public class BoletoCanceladoNoEmissorException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_CANCELADO_NO_EMISSOR";

    public BoletoCanceladoNoEmissorException(String idBoleto) {
        super(CODIGO, "O emissor cancelou o boleto %s depois do envio ao banco".formatted(idBoleto));
    }
}
