package com.example.crud;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/produtos")
public class ProdutoResource {

    @Inject
    ProdutoRepository repository;

    @GET
    public List<Produto> listar() {
        return repository.listAll();
    }

    @GET
    @Path("/{id}")
    public Produto buscar(Long id) {
        return repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
    }

    @POST
    @Transactional
    public Response criar(Produto produto) {
        produto.id = null;
        repository.persist(produto);
        return Response.status(Response.Status.CREATED).entity(produto).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Produto atualizar(Long id, Produto dados) {
        Produto produto = buscar(id);
        produto.nome = dados.nome;
        produto.descricao = dados.descricao;
        return produto;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void remover(Long id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException();
        }
    }
}
