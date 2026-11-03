package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.enums.StatusBoleto;
import com.example.boleto.domain.repository.FiltroBoleto;

public record ListarBoletosInput(String documentoPagador, StatusBoleto status, int pagina, int tamanho) {

    public FiltroBoleto filtro() {
        return new FiltroBoleto(documentoPagador, status);
    }
}
