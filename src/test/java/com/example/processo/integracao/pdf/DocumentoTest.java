package com.example.processo.integracao.pdf;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentoTest {

    static ProcessoVeicular processoAberto() {
        var processo = new ProcessoVeicular("Ana Souza", new Cpf("52998224725"), new Valor("10"), "ABC1D23", 2022,
                new BigDecimal("50000.00"));
        processo.abrir("VEI-2026-000007", LocalDate.of(2026, 10, 19));
        return processo;
    }

    @Test
    void pedidoNoFormatoDoSistemaDePdf() {
        assertEquals(new Documento("abertura-processo-veicular", "VEI-2026-000007", "VEICULAR", "Ana Souza",
                        "52998224725", new BigDecimal("10.00"), LocalDate.of(2026, 10, 19),
                        Map.of("placa", "ABC1D23", "anoFabricacao", 2022, "valorFipe", new BigDecimal("50000.00"))),
                Documento.deAbertura(processoAberto()));
    }

    @Test
    void camposDaModalidadeSaoAsColunasDaTabelaFilhaComEnumPeloNome() {
        var processo = new ProcessoImobiliario("Ana Souza", new Cpf("52998224725"), new Valor("10"), "123",
                TipoImovel.COMERCIAL, new BigDecimal("500000.00"), "SP");
        processo.abrir("IMO-2026-000001", LocalDate.of(2026, 10, 19));

        assertEquals(Map.of("matricula", "123", "tipoImovel", "COMERCIAL", "valorAvaliacao", new BigDecimal("500000.00"),
                "uf", "SP"), Documento.deAbertura(processo).campos());
    }
}
