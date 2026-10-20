package com.example.processo.impressao.dto;

import com.example.processo.core.motor.domain.model.Processo;

/**
 * Pedido ao sistema de PDF: qual modelo usar, a referência do documento e os dados que preenchem o modelo.
 * Genérico para todos os segmentos: os dados próprios de cada um seguem em {@code processo.dados}.
 */
public record Documento(String modelo, String referencia, Processo<?> processo) {
}
