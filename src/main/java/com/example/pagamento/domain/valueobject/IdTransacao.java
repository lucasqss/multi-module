package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.IdTransacaoInvalidaException;

import java.util.Objects;
import java.util.UUID;

/**
 * Id de um envio ao banco. Somos NÓS que geramos ({@code <id do pagamento>-<número do envio>}), então ele
 * carrega o pagamento: qualquer retorno do banco, mesmo de um envio antigo, leva ao pagamento certo.
 */
public record IdTransacao(PagamentoId pagamento, int envio) {

    public IdTransacao {
        Objects.requireNonNull(pagamento, "pagamento é obrigatório");
        if (envio < 1) {
            throw new IllegalArgumentException("envio começa em 1");
        }
    }

    /** Lê o formato que enviamos ao banco. */
    public static IdTransacao de(String valor) {
        try {
            var separador = valor.lastIndexOf('-');
            return new IdTransacao(new PagamentoId(UUID.fromString(valor.substring(0, separador))),
                    Integer.parseInt(valor.substring(separador + 1)));
        } catch (RuntimeException e) { // nulo, sem '-', UUID ou número inválidos
            throw new IdTransacaoInvalidaException(valor);
        }
    }

    @Override
    public String toString() {
        return pagamento + "-" + envio;
    }
}
