package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Processo veicular completo: campos do pai e da filha. Plano e top-level, como o {@link DetalheImobiliario}. */
public record DetalheVeicular(UUID id, String numero, String proponenteNome, String proponenteCpf,
                              BigDecimal valor, Situacao situacao, String motivoRecusa,
                              LocalDate dataAbertura, String placa, Integer anoFabricacao, BigDecimal valorFipe) {
}
