package com.example.boleto.application.boleto.dto;

import com.example.boleto.adapter.database.FiltroBoleto;
import com.example.boleto.domain.state.StatusBoleto;

public record ListarBoletosInput(String documentoPagador, StatusBoleto status, int pagina, int tamanho) {

    public FiltroBoleto filtro() {
        return new FiltroBoleto(documentoPagador, status);
    }
}
