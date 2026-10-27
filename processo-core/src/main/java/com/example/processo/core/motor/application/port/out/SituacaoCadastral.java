package com.example.processo.core.motor.application.port.out;

import com.example.processo.core.motor.domain.model.Cpf;

/**
 * Consulta a um sistema externo, feita no ato da criação de qualquer processo. O motor só conhece este port; a
 * implementação (cliente, DTOs, erros do sistema externo) fica no módulo integracoes.
 */
public interface SituacaoCadastral {

    boolean regular(Cpf cpf);
}
