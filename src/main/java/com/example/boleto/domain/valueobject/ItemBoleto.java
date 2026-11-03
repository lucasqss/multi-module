package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.ItemBoletoInvalidoException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Uma linha da composição do valor do boleto (ex.: "Mensalidade de março", R$ 450,00).
 * É value object porque a seção de itens é sempre substituída inteira: um item não tem
 * identidade nem ciclo de vida próprio fora do boleto.
 */
public record ItemBoleto(String descricao, Dinheiro valor) {

    public static final int TAMANHO_MAXIMO_DESCRICAO = 100;

    public ItemBoleto {
        if (descricao == null || descricao.isBlank()) {
            throw new ItemBoletoInvalidoException("descrição é obrigatória");
        }
        descricao = descricao.strip();
        if (descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ItemBoletoInvalidoException(
                    "descrição deve ter no máximo %d caracteres".formatted(TAMANHO_MAXIMO_DESCRICAO));
        }
        Objects.requireNonNull(valor, "valor é obrigatório");
    }

    public static ItemBoleto de(String descricao, BigDecimal valor) {
        return new ItemBoleto(descricao, new Dinheiro(valor));
    }
}
