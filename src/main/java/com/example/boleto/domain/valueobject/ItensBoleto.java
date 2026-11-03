package com.example.boleto.domain.valueobject;

import java.math.BigDecimal;
import java.util.List;

/** Seção de itens do boleto. O valor do boleto é a soma dos itens. */
public record ItensBoleto(List<ItemBoleto> itens) {

    public static final ItensBoleto VAZIO = new ItensBoleto(List.of());

    public ItensBoleto {
        itens = List.copyOf(itens);
    }

    public Dinheiro total() {
        return new Dinheiro(itens.stream().map(i -> i.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public int quantidade() {
        return itens.size();
    }

    public boolean vazio() {
        return itens.isEmpty();
    }
}
