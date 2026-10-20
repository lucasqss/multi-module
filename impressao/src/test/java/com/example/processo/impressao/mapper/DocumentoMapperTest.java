package com.example.processo.impressao.mapper;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentoMapperTest {

    record DadosAuto(String placa) implements DadosDoSegmento {
    }

    @Test
    void modeloEscolhidoPeloSegmentoEReferenciaPeloNumero() {
        var processo = Processo.abrir("AUT-2026-000007", "AUTO",
                new Solicitacao<>(new Proponente("Ana Souza", "52998224725"), BigDecimal.TEN, new DadosAuto("ABC1D23")),
                LocalDate.of(2026, 10, 19));

        var documento = DocumentoMapper.deAbertura(processo);

        assertEquals("abertura-processo-auto", documento.modelo());
        assertEquals("AUT-2026-000007", documento.referencia());
        assertEquals(processo, documento.processo());
    }
}
