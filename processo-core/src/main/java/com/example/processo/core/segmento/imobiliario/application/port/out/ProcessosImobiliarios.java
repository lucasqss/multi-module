package com.example.processo.core.segmento.imobiliario.application.port.out;

import com.example.processo.core.motor.application.port.spi.PersistenciaDoSegmento;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

/** Gravar e ler (pai PROCESSO + filha PROCESSO_IMOBILIARIO) e a consulta própria do segmento. */
public interface ProcessosImobiliarios extends PersistenciaDoSegmento<DadosImobiliarios> {

    /** Em andamento = aberto ou em processamento. */
    boolean existeEmAndamentoParaMatricula(String matricula, String uf);
}
