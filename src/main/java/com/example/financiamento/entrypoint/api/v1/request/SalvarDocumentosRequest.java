package com.example.financiamento.entrypoint.api.v1.request;

import com.example.financiamento.application.financiamento.dto.SalvarDocumentosInput;
import com.example.financiamento.application.financiamento.dto.SalvarDocumentosInput.DocumentoInput;
import com.example.financiamento.domain.valueobject.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** A seção inteira. Lista vazia limpa a seção. */
public record SalvarDocumentosRequest(@NotNull List<@Valid @NotNull DocumentoRequest> documentos) {

    public record DocumentoRequest(@NotNull TipoDocumento tipo, @NotBlank String referencia) {
    }

    public SalvarDocumentosInput paraInput(UUID id) {
        return new SalvarDocumentosInput(id, documentos.stream().map(d -> new DocumentoInput(d.tipo(), d.referencia())).toList());
    }
}
