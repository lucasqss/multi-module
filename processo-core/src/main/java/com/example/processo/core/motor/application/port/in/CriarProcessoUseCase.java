package com.example.processo.core.motor.application.port.in;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Solicitacao;

/** Abre um processo de qualquer segmento: o segmento é escolhido pela classe dos dados da solicitação. */
public interface CriarProcessoUseCase {

    <D extends DadosDoSegmento> Processo<D> criar(Solicitacao<D> solicitacao);
}
