package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Pagador;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entrada do caso de uso, independente do protocolo (REST, fila, IIB...). */
public record EmitirBoletoInput(
        String nomePagador,
        String documentoPagador,
        String emailPagador,
        BigDecimal valor,
        LocalDate dataVencimento) {

    public DadosBoleto dadosBoleto() {
        return DadosBoleto.de(Pagador.de(nomePagador, documentoPagador, emailPagador), valor, dataVencimento);
    }
}
