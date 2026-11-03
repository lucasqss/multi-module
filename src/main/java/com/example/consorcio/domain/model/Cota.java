package com.example.consorcio.domain.model;

import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;

import java.util.Objects;

/**
 * Visão da cota que o processo de pagamento precisa. Os dados vêm de outro sistema
 * (ver adapter.cota); aqui ela é só um modelo de leitura, mas ainda com comportamento.
 */
public final class Cota {

    private final IdentificadorCota identificador;
    private final SituacaoCota situacao;
    private final Dinheiro saldoDevedor;

    public Cota(IdentificadorCota identificador, SituacaoCota situacao, Dinheiro saldoDevedor) {
        this.identificador = Objects.requireNonNull(identificador);
        this.situacao = Objects.requireNonNull(situacao);
        this.saldoDevedor = Objects.requireNonNull(saldoDevedor);
    }

    public boolean aceitaPagamento() {
        return situacao.aceitaPagamento();
    }

    public boolean comportaPagamentoDe(Dinheiro valor) {
        return !valor.maiorQue(saldoDevedor);
    }

    public IdentificadorCota identificador() {
        return identificador;
    }

    public SituacaoCota situacao() {
        return situacao;
    }

    public Dinheiro saldoDevedor() {
        return saldoDevedor;
    }
}
