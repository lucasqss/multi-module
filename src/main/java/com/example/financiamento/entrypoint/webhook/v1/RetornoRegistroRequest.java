package com.example.financiamento.entrypoint.webhook.v1;

import com.example.financiamento.application.financiamento.dto.RetornoRegistroInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;

/**
 * Contrato definido pelo ÓRGÃO (não por nós): situação em texto, data/hora com fuso.
 * A tradução para o nosso formato fica aqui, na borda.
 */
public record RetornoRegistroRequest(
        @NotBlank String protocolo,
        @NotNull @Pattern(regexp = "REGISTRADO|RECUSADO") String situacao,
        String identificacaoRegistrada,
        String numeroRegistro,
        String motivoRecusa,
        @NotNull OffsetDateTime dataHora) {

    public RetornoRegistroInput paraInput() {
        return new RetornoRegistroInput(protocolo, "REGISTRADO".equals(situacao), identificacaoRegistrada,
                numeroRegistro, motivoRecusa, dataHora.toInstant());
    }
}
