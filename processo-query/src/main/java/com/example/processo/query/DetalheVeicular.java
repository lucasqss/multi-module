package com.example.processo.query;

import com.example.jpacore.SituacaoProcesso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Processo veicular completo: campos do pai e da filha. Plano e top-level, como o {@link DetalheImobiliario}. */
public record DetalheVeicular(UUID id, String numero, String proponenteNome, String proponenteCpf,
                              BigDecimal valor, SituacaoProcesso situacao, String motivoRecusa,
                              LocalDate dataAbertura, String placa, int anoFabricacao, BigDecimal valorFipe) {
}
