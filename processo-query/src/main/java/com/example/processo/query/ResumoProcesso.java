package com.example.processo.query;

import com.example.jpacore.SituacaoProcesso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Linha da listagem: só os campos do pai, iguais para todos os segmentos. */
public record ResumoProcesso(UUID id, String numero, String segmento, String proponente, BigDecimal valor,
                             SituacaoProcesso situacao, LocalDate dataAbertura) {
}
