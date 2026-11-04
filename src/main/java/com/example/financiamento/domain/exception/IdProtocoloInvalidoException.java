package com.example.financiamento.domain.exception;

/** Protocolo fora do formato que nós geramos ({@code <id do financiamento>-<número do envio>}). */
public class IdProtocoloInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "ID_PROTOCOLO_INVALIDO";

    public IdProtocoloInvalidoException(String valor) {
        super(CODIGO, "Protocolo inválido: " + valor);
    }
}
