package com.example.processo.modelo;

/**
 * Um sistema externo consultado pelo negócio não respondeu (fora do ar, erro, timeout). Lançada pela camada de
 * integração, que traduz os erros técnicos do cliente HTTP: o negócio e a apresentação não conhecem REST Client.
 */
public class SistemaExternoIndisponivelException extends RuntimeException {

    public SistemaExternoIndisponivelException(String sistema, Throwable causa) {
        super("Sistema " + sistema + " indisponível; tente novamente mais tarde", causa);
    }
}
