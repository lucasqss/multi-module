package com.example.financiamento.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/** Um documento entregue: o tipo e a referência dele (número ou id do arquivo no repositório de documentos). */
@Embeddable
public record Documento(@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) TipoDocumento tipo,
                        @Column(nullable = false, length = 100) String referencia) {

    public Documento {
        Objects.requireNonNull(tipo, "tipo do documento é obrigatório");
        Objects.requireNonNull(referencia, "referência do documento é obrigatória");
    }
}
