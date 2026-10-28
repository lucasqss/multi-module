package com.example.processo.integracao.pdf;

import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** O pedido enviado e a tradução dos erros; o cliente é substituído por um falso. A chamada HTTP real é coberta nos ITs. */
class PdfIntegracaoTest {

    @Test
    void enviaODocumentoDeAbertura() {
        var processo = DocumentoTest.processoAberto();
        var enviados = new ArrayList<Documento>();

        new PdfIntegracao(enviados::add).gerarAbertura(processo);

        assertEquals(List.of(Documento.deAbertura(processo)), enviados);
    }

    @Test
    void falhaDoClienteViraIndisponibilidade() {
        var processo = DocumentoTest.processoAberto();
        var foraDoAr = new PdfIntegracao(documento -> {
            throw new ProcessingException("Connection refused");
        });
        var erroHttp = new PdfIntegracao(documento -> {
            throw new WebApplicationException(500);
        });

        assertThrows(SistemaExternoIndisponivelException.class, () -> foraDoAr.gerarAbertura(processo));
        assertThrows(SistemaExternoIndisponivelException.class, () -> erroHttp.gerarAbertura(processo));
    }
}
