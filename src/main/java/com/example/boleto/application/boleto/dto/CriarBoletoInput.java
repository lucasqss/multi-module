package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.Pagador;

import java.time.LocalDate;

/** Entrada do caso de uso, independente do protocolo (REST, fila, IIB...). Só o mínimo do rascunho. */
public record CriarBoletoInput(String nomePagador, String documentoPagador, String emailPagador,
                               LocalDate dataVencimento) {

    public Pagador pagador() {
        return Pagador.de(nomePagador, documentoPagador, emailPagador);
    }
}
