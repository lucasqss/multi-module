package com.example.boleto.domain.model;

import com.example.boleto.domain.valueobject.DadosBoleto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO avaliado pelas specifications de emissão/alteração.
 *
 * <p>Reúne tudo o que as regras precisam saber, já resolvido: os dados pedidos, o momento da
 * solicitação, parâmetros configuráveis e fatos consultados no repositório.
 * Assim as specifications são funções puras: sem banco, sem relógio, sem framework.
 *
 * <p>Criada por {@code SolicitacaoBoletoFactory}.
 */
public record SolicitacaoBoleto(
        DadosBoleto dados,
        Instant momento,
        LocalDate hoje,
        int prazoMaximoVencimentoDias,
        long boletosEmAbertoDoPagador) {

    public SolicitacaoBoleto {
        Objects.requireNonNull(dados);
        Objects.requireNonNull(momento);
        Objects.requireNonNull(hoje);
    }
}
