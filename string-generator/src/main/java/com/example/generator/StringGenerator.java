package com.example.generator;

import com.example.crud.Produto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class StringGenerator {

    /** Concatena todas as strings (nome e descrição) dos produtos recebidos. */
    public String concatenar(Collection<Produto> produtos) {
        return produtos.stream()
                .flatMap(p -> Stream.of(p.nome, p.descricao))
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }
}
