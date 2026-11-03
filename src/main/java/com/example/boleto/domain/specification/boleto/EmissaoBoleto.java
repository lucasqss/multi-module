package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.valueobject.Pagador;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO das regras de EMISSÃO (completude): o boleto como está ao final da edição
 * + as condições do momento. Montado pela própria entidade em {@code Boleto.emitir(...)}.
 * Specs puras: tudo que precisam está aqui.
 */
public record EmissaoBoleto(Pagador pagador, ItensBoleto itens, LocalDate dataVencimento, CondicoesEmissao condicoes) {

    public EmissaoBoleto {
        Objects.requireNonNull(pagador);
        Objects.requireNonNull(itens);
        Objects.requireNonNull(dataVencimento);
        Objects.requireNonNull(condicoes);
    }

    public Dinheiro valor() {
        return itens.total();
    }

    public LocalDate hoje() {
        return condicoes.hoje();
    }
}
