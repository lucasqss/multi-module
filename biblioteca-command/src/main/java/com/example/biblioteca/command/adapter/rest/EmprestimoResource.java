package com.example.biblioteca.command.adapter.rest;

import com.example.biblioteca.command.application.CirculacaoService;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.UUID;

@Path("/emprestimos")
public class EmprestimoResource {

    public record NovoEmprestimo(UUID leitorId, UUID exemplarId) {
    }

    public record Renovado(LocalDate dataPrevista) {
    }

    private final CirculacaoService circulacao;

    EmprestimoResource(CirculacaoService circulacao) {
        this.circulacao = circulacao;
    }

    @POST
    public Response emprestar(NovoEmprestimo e) {
        return Criado.em("/emprestimos", circulacao.emprestar(e.leitorId(), e.exemplarId()));
    }

    @POST
    @Path("{id}/renovacao")
    public Renovado renovar(@PathParam("id") UUID id) {
        return new Renovado(circulacao.renovar(id));
    }

    @POST
    @Path("{id}/devolucao")
    public void devolver(@PathParam("id") UUID id) {
        circulacao.devolver(id);
    }
}
