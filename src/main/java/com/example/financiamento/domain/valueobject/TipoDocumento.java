package com.example.financiamento.domain.valueobject;

/** Documentos aceitos (exigência SIMPLIFICADA, de exemplo). Quais são obrigatórios: {@code RegrasFinanciamento}. */
public enum TipoDocumento {
    IDENTIDADE,
    COMPROVANTE_RENDA,
    /** Só imóvel. */
    MATRICULA_IMOVEL,
    /** Só veículo: documento do veículo. */
    CRLV
}
