package com.example.pagamento.shared;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado genérico, usado do repositório até a resposta HTTP. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public long totalPaginas() {
        return tamanho == 0 ? 0 : (total + tamanho - 1) / tamanho;
    }

    public <R> Pagina<R> map(Function<? super T, ? extends R> conversor) {
        return new Pagina<>(itens.stream().<R>map(conversor).toList(), pagina, tamanho, total);
    }
}
