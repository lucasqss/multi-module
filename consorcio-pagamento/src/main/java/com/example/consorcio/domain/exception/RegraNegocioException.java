package com.example.consorcio.domain.exception;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Uma ou mais regras de negócio foram violadas.
 * Carrega TODAS as violações encontradas, para o cliente corrigir tudo de uma vez.
 */
public class RegraNegocioException extends DomainException {

    private final List<Violacao> violacoes;

    public RegraNegocioException(List<Violacao> violacoes) {
        super(violacoes.size() == 1 ? violacoes.getFirst().codigo() : "REGRAS_DE_NEGOCIO_VIOLADAS",
                violacoes.stream().map(Violacao::mensagem).collect(Collectors.joining("; ")));
        this.violacoes = List.copyOf(violacoes);
    }

    public RegraNegocioException(String codigo, String mensagem) {
        this(List.of(new Violacao(codigo, mensagem)));
    }

    public List<Violacao> getViolacoes() {
        return violacoes;
    }
}
