package com.example.processo.integracao.pdf;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente do sistema externo de geração de PDF; o endereço vem de quarkus.rest-client.sistema-pdf.url. Fora do perfil
 * dev é sempre esta chamada HTTP; no dev, o {@link PdfClientSimulado} toma o lugar dela.
 */
@RegisterRestClient(configKey = "sistema-pdf")
@Path("/documentos")
public interface PdfClient {

    @POST
    void gerar(Documento documento);
}
