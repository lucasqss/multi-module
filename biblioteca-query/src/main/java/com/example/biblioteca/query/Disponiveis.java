package com.example.biblioteca.query;

import java.util.UUID;

/**
 * Projeção de {@code select new}: precisa ser top-level, pois o Hibernate instancia pelo nome canônico e
 * um record aninhado (AcervoQueries.Disponiveis) não é encontrado em runtime — e a compilação não acusa.
 */
public record Disponiveis(UUID livroId, long quantidade) {
}
