package com.example.processo.integracoes.pdf.adapter;

import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.integracoes.pdf.dto.Documento;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** O pedido enviado e a tradução dos erros; o cliente é substituído por um falso. A chamada HTTP real é coberta nos ITs. */
class GeradorDeDocumentosAdapterTest {

    record DadosAuto(String placa) implements DadosDoSegmento {
    }

    static final Processo<DadosAuto> PROCESSO = Processo.abrir("AUT-2026-000007", "AUTO",
            new Solicitacao<>(new Proponente("Ana Souza", "52998224725"), new Valor("10"), new DadosAuto("ABC1D23")),
            LocalDate.of(2026, 10, 19));

    @Test
    void enviaODocumentoDeAbertura() {
        var enviados = new ArrayList<Documento>();

        new GeradorDeDocumentosAdapter(enviados::add).gerarAbertura(PROCESSO);

        assertEquals(List.of(Documento.deAbertura(PROCESSO)), enviados);
    }

    @Test
    void falhaDoClienteViraIndisponibilidadeDoDominio() {
        var foraDoAr = new GeradorDeDocumentosAdapter(documento -> {
            throw new ProcessingException("Connection refused");
        });
        var erroHttp = new GeradorDeDocumentosAdapter(documento -> {
            throw new WebApplicationException(500);
        });

        assertThrows(SistemaExternoIndisponivelException.class, () -> foraDoAr.gerarAbertura(PROCESSO));
        assertThrows(SistemaExternoIndisponivelException.class, () -> erroHttp.gerarAbertura(PROCESSO));
    }
}
