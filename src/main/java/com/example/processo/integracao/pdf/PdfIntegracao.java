package com.example.processo.integracao.pdf;

import com.example.processo.modelo.Processo;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** O que o negócio chama para pedir o PDF de abertura: monta o pedido no formato do sistema e traduz os erros. */
@ApplicationScoped
public class PdfIntegracao {

    private final PdfClient cliente;

    public PdfIntegracao(@RestClient PdfClient cliente) {
        this.cliente = cliente;
    }

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira indisponibilidade. */
    public void gerarAbertura(Processo processo) {
        try {
            cliente.gerar(Documento.deAbertura(processo));
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de PDF", e);
        }
    }
}
