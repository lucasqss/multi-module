package com.example.pagamento.entrypoint.webhook.v1;

import com.example.pagamento.application.pagamento.dto.RetornoBancoInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Contrato definido pelo BANCO (não por nós): situação em texto, valor em centavos, data/hora com fuso.
 * A tradução para o nosso formato fica aqui, na borda; o domínio nunca vê centavos.
 */
public record RetornoBancoRequest(
        @NotBlank String idTransacao,
        @NotNull @Pattern(regexp = "LIQUIDADO|RECUSADO") String situacao,
        Long valorLiquidadoCentavos,
        String autenticacao,
        String motivoRecusa,
        @NotNull OffsetDateTime dataHora) {

    public RetornoBancoInput paraInput() {
        return new RetornoBancoInput(idTransacao, "LIQUIDADO".equals(situacao),
                valorLiquidadoCentavos == null ? null : BigDecimal.valueOf(valorLiquidadoCentavos).movePointLeft(2),
                autenticacao, motivoRecusa, dataHora.toInstant());
    }
}
