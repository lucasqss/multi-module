package com.example.financiamento.domain.valueobject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Seção "documentos": substituída inteira a cada salvamento. */
public record DocumentosDoFinanciamento(List<Documento> documentos) {

    public static final DocumentosDoFinanciamento NENHUM = new DocumentosDoFinanciamento(List.of());

    public DocumentosDoFinanciamento {
        documentos = List.copyOf(documentos);
    }

    public boolean possuiRepetidos() {
        return documentos.stream().map(Documento::tipo).distinct().count() != documentos.size();
    }

    /** O primeiro dos exigidos que não foi entregue. */
    public Optional<TipoDocumento> primeiroFaltante(Collection<TipoDocumento> exigidos) {
        var entregues = documentos.stream().map(Documento::tipo).toList();
        return exigidos.stream().filter(tipo -> !entregues.contains(tipo)).findFirst();
    }
}
