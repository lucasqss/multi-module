package com.example.processo.core.motor.domain.state;

/** O valor persistido (PROCESSO.SITUACAO) e serializado. O comportamento de cada um está no seu {@link EstadoDoProcesso}. */
public enum Situacao { ABERTO, EM_PROCESSAMENTO, CONCLUIDO, RECUSADO, CANCELADO }
