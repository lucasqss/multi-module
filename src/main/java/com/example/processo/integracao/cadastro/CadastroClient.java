package com.example.processo.integracao.cadastro;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente do sistema externo de cadastro; o endereço vem de quarkus.rest-client.cadastro.url. Fora do perfil dev
 * é sempre esta chamada HTTP; no dev, o {@link CadastroClientSimulado} toma o lugar dela.
 */
@RegisterRestClient(configKey = "cadastro")
@Path("/situacoes")
public interface CadastroClient {

    @GET
    @Path("{documento}")
    RespostaCadastro consultar(@PathParam("documento") String documento);
}
