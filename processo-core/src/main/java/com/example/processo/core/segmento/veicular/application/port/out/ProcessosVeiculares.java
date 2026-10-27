package com.example.processo.core.segmento.veicular.application.port.out;

import com.example.processo.core.motor.application.port.spi.PersistenciaDoSegmento;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

/** Gravar e ler (pai PROCESSO + filha PROCESSO_VEICULAR). Consultas próprias do segmento entram aqui. */
public interface ProcessosVeiculares extends PersistenciaDoSegmento<DadosVeiculares> {
}
