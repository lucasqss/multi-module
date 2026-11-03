package com.example.consorcio.domain.model;

import com.example.consorcio.domain.valueobject.DadosPagamento;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO avaliado pelas specifications de abertura/alteração.
 *
 * <p>Reúne tudo o que as regras precisam saber, já resolvido: a cota (vinda de outro sistema),
 * os dados pedidos, "hoje", parâmetros configuráveis e fatos consultados no repositório.
 * Assim as specifications são funções puras: sem banco, sem relógio, sem framework.
 */
public record SolicitacaoPagamento(
        Cota cota,
        DadosPagamento dados,
        LocalDate hoje,
        int prazoMaximoVencimentoDias,
        boolean cotaPossuiProcessoEmAndamento) {

    public SolicitacaoPagamento {
        Objects.requireNonNull(cota);
        Objects.requireNonNull(dados);
        Objects.requireNonNull(hoje);
    }
}
