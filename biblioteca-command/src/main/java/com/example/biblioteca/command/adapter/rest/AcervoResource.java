package com.example.biblioteca.command.adapter.rest;

import com.example.biblioteca.command.application.AcervoService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/")
public class AcervoResource {

    public record NovoExemplar(String tombo) {
    }

    private final AcervoService acervo;

    AcervoResource(AcervoService acervo) {
        this.acervo = acervo;
    }

    @POST
    @Path("livros")
    public Response cadastrarLivro(AcervoService.NovoLivro livro) {
        return Criado.em("/livros", acervo.cadastrarLivro(livro));
    }

    @POST
    @Path("livros/{livroId}/exemplares")
    public Response registrarExemplar(@PathParam("livroId") UUID livroId, NovoExemplar exemplar) {
        return Criado.em("/exemplares", acervo.registrarExemplar(livroId, exemplar.tombo()));
    }

    /** Baixa (descarte) do exemplar: ele deixa de circular, mas o histórico é mantido. */
    @DELETE
    @Path("exemplares/{id}")
    public void baixarExemplar(@PathParam("id") UUID id) {
        acervo.baixarExemplar(id);
    }
}
