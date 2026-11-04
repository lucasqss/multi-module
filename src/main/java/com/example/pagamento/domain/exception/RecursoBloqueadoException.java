package com.example.pagamento.domain.exception;

import java.util.UUID;

/**
 * Outra operação está alterando o mesmo pagamento (lock pessimista) e o tempo de espera acabou.
 * O repositório traduz a exceção de JPA para esta; o cliente pode tentar de novo.
 */
public class RecursoBloqueadoException extends ConflitoException {

    public static final String CODIGO = "RECURSO_BLOQUEADO";

    public RecursoBloqueadoException(UUID id) {
        super(CODIGO, "O pagamento %s está sendo alterado por outra operação. Tente de novo.".formatted(id));
    }
}
