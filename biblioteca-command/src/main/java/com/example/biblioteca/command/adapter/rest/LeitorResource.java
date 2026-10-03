package com.example.biblioteca.command.adapter.rest;

import com.example.biblioteca.command.application.LeitorService;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/leitores")
public class LeitorResource {

    public record NovoLeitor(String nome, String email) {
    }

    private final LeitorService leitores;

    LeitorResource(LeitorService leitores) {
        this.leitores = leitores;
    }

    @POST
    public Response cadastrar(NovoLeitor leitor) {
        return Criado.em("/leitores", leitores.cadastrar(leitor.nome(), leitor.email()));
    }
}
