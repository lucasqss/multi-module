package com.example.pagamento.entrypoint.api.v1.response;

import com.example.pagamento.shared.Pagina;

import java.util.List;
import java.util.function.Function;

/** Contrato HTTP de listagem paginada. */
public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long total, long totalPaginas) {

    public static <O, T> PaginaResponse<T> de(Pagina<O> pagina, Function<? super O, ? extends T> conversor) {
        Pagina<T> convertida = pagina.<T>map(conversor);
        return new PaginaResponse<>(convertida.itens(), convertida.pagina(), convertida.tamanho(),
                convertida.total(), convertida.totalPaginas());
    }
}
