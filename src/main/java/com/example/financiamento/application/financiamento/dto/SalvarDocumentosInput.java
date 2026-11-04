package com.example.financiamento.application.financiamento.dto;

import com.example.financiamento.domain.valueobject.Documento;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.TipoDocumento;

import java.util.List;
import java.util.UUID;

/** A seção de documentos inteira. */
public record SalvarDocumentosInput(UUID id, List<DocumentoInput> itens) {

    public record DocumentoInput(TipoDocumento tipo, String referencia) {
    }

    public DocumentosDoFinanciamento documentos() {
        return new DocumentosDoFinanciamento(itens.stream().map(d -> new Documento(d.tipo(), d.referencia())).toList());
    }
}
