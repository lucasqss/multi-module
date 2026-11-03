package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.enums.TipoDocumento;
import com.example.boleto.domain.exception.DocumentoInvalidoException;

/**
 * CPF (11 dígitos) ou CNPJ (14 dígitos), só números.
 * Validação FAKE: confere apenas o tamanho, não os dígitos verificadores.
 */
public record Documento(String numero) {

    public Documento {
        if (numero == null || !numero.matches("\\d{11}|\\d{14}")) {
            throw new DocumentoInvalidoException("informe CPF (11 dígitos) ou CNPJ (14 dígitos), só números");
        }
    }

    public TipoDocumento tipo() {
        return numero.length() == 11 ? TipoDocumento.CPF : TipoDocumento.CNPJ;
    }

    public boolean pessoaFisica() {
        return tipo() == TipoDocumento.CPF;
    }

    /** Mascarado: o documento completo nunca vai parar em log ou mensagem de erro (LGPD). */
    @Override
    public String toString() {
        return "*".repeat(numero.length() - 4) + numero.substring(numero.length() - 4);
    }
}
