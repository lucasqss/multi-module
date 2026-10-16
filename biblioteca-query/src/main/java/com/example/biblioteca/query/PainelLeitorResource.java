package com.example.biblioteca.query;

import com.example.stringgenerator.CitacaoAbnt;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @Path de classe completo e não "/" + método: no deploy único o command já tem @Path("/leitores"), e o JAX-RS
 * escolhe a classe raiz mais específica antes de olhar os métodos (não volta atrás entre classes).
 */
@Path("/leitores/{id}/painel")
public class PainelLeitorResource {

    public record ItemPainel(UUID emprestimoId, String tombo, String titulo, String citacao,
                             LocalDate dataPrevista, int renovacoes, boolean atrasado) {
    }

    public record PainelLeitor(UUID leitorId, String nome, LocalDate suspensoAte, List<ItemPainel> emprestimos) {
    }

    private final CirculacaoQueries circulacao;
    private final Obras obras;

    PainelLeitorResource(CirculacaoQueries circulacao, Obras obras) {
        this.circulacao = circulacao;
        this.obras = obras;
    }

    @GET
    public PainelLeitor painel(@PathParam("id") UUID leitorId) {
        var leitor = circulacao.leitor(leitorId).orElseThrow(NotFoundException::new);
        var hoje = LocalDate.now();
        var abertos = circulacao.abertosDo(leitorId);
        var obrasDosLivros = obras.dosEmprestimos(abertos);
        var itens = abertos.stream()
                .map(e -> new ItemPainel(e.getId(), e.getExemplar().getTombo(), e.getExemplar().getLivro().getTitulo(),
                        CitacaoAbnt.de(obrasDosLivros.get(e.getExemplar().getLivroId())),
                        e.getDataPrevista(), e.getRenovacoes(), hoje.isAfter(e.getDataPrevista())))
                .toList();
        return new PainelLeitor(leitor.getId(), leitor.getNome(), leitor.getSuspensoAte(), itens);
    }
}
