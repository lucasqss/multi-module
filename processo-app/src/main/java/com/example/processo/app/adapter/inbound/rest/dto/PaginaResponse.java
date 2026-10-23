package com.example.processo.app.adapter.inbound.rest.dto;

import jakarta.data.page.Page;

import java.util.List;

/** Uma página da listagem: os itens, onde se está e quantos existem no total. {@code pagina} começa em 1. */
public record PaginaResponse<T>(List<T> itens, long pagina, int tamanho, long total) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.content(), page.pageRequest().page(), page.pageRequest().size(),
                page.totalElements());
    }
}
