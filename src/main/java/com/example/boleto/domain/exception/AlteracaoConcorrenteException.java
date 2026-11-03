package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.BoletoId;

/** O boleto foi alterado por outra requisição entre a leitura e a gravação (lock otimista). O adapter traduz a exceção de JPA para esta, então application e entrypoint não conhecem JPA. */
public class AlteracaoConcorrenteException extends ConflitoException {

    public static final String CODIGO = "ALTERACAO_CONCORRENTE";

    public AlteracaoConcorrenteException(BoletoId id) {
        super(CODIGO, "O boleto %s foi alterado por outra operação. Consulte novamente e repita.".formatted(id));
    }
}
