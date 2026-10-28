package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Linha da listagem: só os campos do pai, iguais para todas as modalidades. */
public record ResumoProcesso(UUID id, String numero, String segmento, String proponente, BigDecimal valor,
                             Situacao situacao, LocalDate dataAbertura) {
}
