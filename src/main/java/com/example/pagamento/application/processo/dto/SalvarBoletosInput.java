package com.example.pagamento.application.processo.dto;

import com.example.pagamento.domain.valueobject.LinhaDigitavel;

import java.util.List;
import java.util.UUID;

/** A seção de boletos inteira: só as linhas digitáveis; os dados vêm do emissor. */
public record SalvarBoletosInput(UUID id, List<String> linhasDigitaveis) {

    public List<LinhaDigitavel> linhas() {
        return linhasDigitaveis.stream().map(LinhaDigitavel::new).toList();
    }
}
