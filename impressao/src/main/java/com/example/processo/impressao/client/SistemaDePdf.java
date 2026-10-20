package com.example.processo.impressao.client;

import com.example.processo.impressao.dto.Documento;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Cliente do sistema de geração de PDF; o endereço vem de quarkus.rest-client.sistema-pdf.url. */
@RegisterRestClient(configKey = "sistema-pdf")
@Path("/documentos")
public interface SistemaDePdf {

    @POST
    void gerar(Documento documento);
}
