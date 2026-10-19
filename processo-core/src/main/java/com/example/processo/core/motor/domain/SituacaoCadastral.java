package com.example.processo.core.motor.domain;

/**
 * Consulta a um sistema externo, feita no ato da criação de qualquer processo. O motor só conhece este port; a
 * implementação (cliente, DTOs, erros do sistema externo) fica no módulo integracoes.
 */
public interface SituacaoCadastral {

    boolean regular(String cpf);
}
