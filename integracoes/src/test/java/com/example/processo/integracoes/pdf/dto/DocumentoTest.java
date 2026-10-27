package com.example.processo.integracoes.pdf.dto;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentoTest {

    enum Cor { AZUL }

    record DadosAuto(String placa, Cor cor) implements DadosDoSegmento {
    }

    @Test
    void pedidoNoFormatoDoSistemaDePdf() {
        var processo = Processo.abrir("AUT-2026-000007", "AUTO",
                new Solicitacao<>(new Proponente("Ana Souza", "52998224725"), BigDecimal.TEN,
                        new DadosAuto("ABC1D23", Cor.AZUL)),
                LocalDate.of(2026, 10, 19));

        var documento = Documento.deAbertura(processo);

        assertEquals(new Documento("abertura-processo-auto", "AUT-2026-000007", "AUTO", "Ana Souza", "52998224725",
                BigDecimal.TEN, LocalDate.of(2026, 10, 19), Map.of("placa", "ABC1D23", "cor", "AZUL")), documento);
    }
}
