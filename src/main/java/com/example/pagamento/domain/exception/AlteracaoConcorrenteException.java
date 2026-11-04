package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;

/**
 * O processo foi alterado por outra requisição entre a leitura e a gravação (lock otimista). O adapter traduz
 * a exceção de JPA para esta.
 */
public class AlteracaoConcorrenteException extends ConflitoException {

    public static final String CODIGO = "ALTERACAO_CONCORRENTE";

    public AlteracaoConcorrenteException(ProcessoPagamentoId id) {
        super(CODIGO, "O processo %s foi alterado por outra operação. Consulte novamente e repita.".formatted(id));
    }
}
