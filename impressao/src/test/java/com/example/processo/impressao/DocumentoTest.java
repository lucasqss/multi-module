package com.example.processo.impressao;

import com.example.processo.core.motor.domain.DadosDoSegmento;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.Solicitacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentoTest {

    record DadosAuto(String placa) implements DadosDoSegmento {
    }

    @Test
    void modeloEscolhidoPeloSegmentoEReferenciaPeloNumero() {
        var processo = Processo.abrir("AUT-2026-000007", "AUTO",
                new Solicitacao<>(new Proponente("Ana Souza", "52998224725"), BigDecimal.TEN, new DadosAuto("ABC1D23")),
                LocalDate.of(2026, 10, 19));

        var documento = Documento.deAbertura(processo);

        assertEquals("abertura-processo-auto", documento.modelo());
        assertEquals("AUT-2026-000007", documento.referencia());
        assertEquals(processo, documento.processo());
    }
}
