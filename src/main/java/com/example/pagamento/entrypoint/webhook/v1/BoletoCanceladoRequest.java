package com.example.pagamento.entrypoint.webhook.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/** Contrato definido pelo SISTEMA EMISSOR: ele identifica o boleto pelo id DELE. */
public record BoletoCanceladoRequest(@NotBlank String idBoleto, @NotNull OffsetDateTime dataHoraCancelamento) {
}
