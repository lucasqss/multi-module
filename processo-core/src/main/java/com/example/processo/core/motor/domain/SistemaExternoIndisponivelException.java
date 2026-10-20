package com.example.processo.core.motor.domain;

/**
 * Um sistema externo consultado pelo negócio não respondeu (fora do ar, erro, timeout). Lançada pelos adapters
 * de integração, que traduzem os erros técnicos do cliente: o motor e o REST não conhecem REST Client.
 */
public class SistemaExternoIndisponivelException extends RuntimeException {

    public SistemaExternoIndisponivelException(String sistema, Throwable causa) {
        super("Sistema " + sistema + " indisponível; tente novamente mais tarde", causa);
    }
}
