package com.example.boleto.domain.exception;

/** Seção de itens com item de valor zero. */
public class ItemComValorZeradoException extends RegraNegocioException {

    public static final String CODIGO = "ITEM_COM_VALOR_ZERADO";

    public ItemComValorZeradoException(String descricao) {
        super(CODIGO, "O item \"%s\" precisa ter valor maior que zero".formatted(descricao));
    }
}
