package com.example.processo.query;

import com.example.jpacore.SituacaoProcesso;
import com.example.jpacore.TipoImovel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Processo imobiliário completo: campos do pai e da filha. Plano e top-level porque o {@code select new} do
 * Hibernate não instancia records aninhados.
 */
public record DetalheImobiliario(UUID id, String numero, String proponenteNome, String proponenteCpf,
                                 BigDecimal valor, SituacaoProcesso situacao, String motivoRecusa,
                                 LocalDate dataAbertura,
                                 String matricula, TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf) {
}
