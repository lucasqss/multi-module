package com.example.pagamento.domain.exception;

/** Id de transação fora do formato que nós geramos ({@code <id do pagamento>-<número do envio>}). */
public class IdTransacaoInvalidaException extends DadoInvalidoException {

    public static final String CODIGO = "ID_TRANSACAO_INVALIDO";

    public IdTransacaoInvalidaException(String valor) {
        super(CODIGO, "Id de transação inválido: " + valor);
    }
}
