package com.example.processo.impressao;

import com.example.processo.core.motor.domain.Processo;

/**
 * Pedido ao sistema de PDF: qual modelo usar, a referência do documento e os dados que preenchem o modelo.
 * Genérico para todos os segmentos: o modelo é escolhido pelo código do segmento e os dados próprios dele seguem
 * em {@code processo.dados}.
 */
public record Documento(String modelo, String referencia, Processo<?> processo) {

    public static Documento deAbertura(Processo<?> processo) {
        return new Documento("abertura-processo-" + processo.segmento().toLowerCase(), processo.numero(), processo);
    }
}
