package com.example.processo.impressao.mapper;

import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.impressao.dto.Documento;

/** Processo → pedido ao sistema de PDF. */
public final class DocumentoMapper {

    private DocumentoMapper() {
    }

    /** O modelo é escolhido pelo código do segmento; a referência é o número do processo. */
    public static Documento deAbertura(Processo<?> processo) {
        return new Documento("abertura-processo-" + processo.segmento().toLowerCase(), processo.numero(), processo);
    }
}
