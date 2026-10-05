package com.example.modelo.app;

import com.example.modelo.core.Saudacao;
import com.example.modelo.core.SaudacaoService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/saudacoes")
@Produces(MediaType.APPLICATION_JSON)
public class SaudacaoResource {

    private final SaudacaoService service;

    public SaudacaoResource(SaudacaoService service) {
        this.service = service;
    }

    @GET
    @Path("/{nome}")
    public Saudacao saudar(@PathParam("nome") String nome) {
        return service.saudar(nome);
    }
}
