package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** A seção de itens inteira (semântica de PUT: substitui o que havia). */
public record SalvarItensBoletoInput(UUID id, List<Item> itens) {

    public record Item(String descricao, BigDecimal valor) {
    }

    public ItensBoleto itensBoleto() {
        return new ItensBoleto(itens.stream().map(i -> ItemBoleto.de(i.descricao(), i.valor())).toList());
    }
}
