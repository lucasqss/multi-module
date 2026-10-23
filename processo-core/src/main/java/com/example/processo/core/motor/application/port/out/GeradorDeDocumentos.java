package com.example.processo.core.motor.application.port.out;

import com.example.processo.core.motor.domain.model.Processo;

/**
 * Pede a um sistema externo o documento (PDF) de abertura de um processo de qualquer segmento. O motor chama depois
 * do commit; a implementação (cliente, DTO, erros do sistema externo) fica no módulo integracoes.
 */
public interface GeradorDeDocumentos {

    void gerarAbertura(Processo<?> processo);
}
