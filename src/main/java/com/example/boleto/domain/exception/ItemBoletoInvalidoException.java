package com.example.boleto.domain.exception;

/** Item com descrição vazia ou longa demais. */
public class ItemBoletoInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "ITEM_BOLETO_INVALIDO";

    public ItemBoletoInvalidoException(String motivo) {
        super(CODIGO, "Item do boleto inválido: " + motivo);
    }
}
