package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.PagamentoId;

/**
 * Outra operação está alterando o mesmo pagamento (lock pessimista) e o tempo de espera acabou.
 * O adapter traduz a exceção de JPA para esta; o cliente pode tentar de novo.
 */
public class RecursoBloqueadoException extends ConflitoException {

    public static final String CODIGO = "RECURSO_BLOQUEADO";

    public RecursoBloqueadoException(PagamentoId id) {
        super(CODIGO, "O pagamento %s está sendo alterado por outra operação. Tente de novo.".formatted(id));
    }
}
