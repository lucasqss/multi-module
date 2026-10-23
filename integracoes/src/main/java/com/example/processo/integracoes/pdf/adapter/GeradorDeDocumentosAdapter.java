package com.example.processo.integracoes.pdf.adapter;

import com.example.processo.core.motor.application.port.out.GeradorDeDocumentos;
import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.integracoes.pdf.client.SistemaDePdf;
import com.example.processo.integracoes.pdf.dto.Documento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** Implementa o port do motor sobre o sistema de PDF: monta o pedido e traduz os erros do cliente. */
@ApplicationScoped
public class GeradorDeDocumentosAdapter implements GeradorDeDocumentos {

    private final SistemaDePdf cliente;

    public GeradorDeDocumentosAdapter(@RestClient SistemaDePdf cliente) {
        this.cliente = cliente;
    }

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira erro do domínio. */
    @Override
    public void gerarAbertura(Processo<?> processo) {
        try {
            cliente.gerar(Documento.deAbertura(processo));
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de PDF", e);
        }
    }
}
