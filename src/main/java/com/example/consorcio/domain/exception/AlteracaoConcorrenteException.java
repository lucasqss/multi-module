package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

/**
 * O processo foi alterado por outra requisição entre a leitura e a gravação (lock otimista).
 * O adapter traduz a exceção de JPA para esta, então application e entrypoint não conhecem JPA.
 * O cliente deve recarregar o processo e tentar de novo.
 */
public class AlteracaoConcorrenteException extends ConflitoException {

    public static final String CODIGO = "ALTERACAO_CONCORRENTE";

    public AlteracaoConcorrenteException(ProcessoPagamentoId id) {
        super(CODIGO, "O processo %s foi alterado por outra operação. Consulte novamente e repita.".formatted(id));
    }
}
