package com.example.generator;

import com.example.crud.ProdutoRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/gerador")
public class StringGeneratorResource {

    // Bean vindo do módulo crud, injetado normalmente via CDI
    @Inject
    ProdutoRepository repository;

    @Inject
    StringGenerator generator;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String concatenarTodos() {
        return generator.concatenar(repository.listAll());
    }
}
