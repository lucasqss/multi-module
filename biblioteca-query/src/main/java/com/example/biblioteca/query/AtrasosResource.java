package com.example.biblioteca.query;

import com.example.stringgenerator.CitacaoAbnt;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** @Path de classe completo pelo mesmo motivo de {@link PainelLeitorResource} (o command tem "/emprestimos"). */
@Path("/emprestimos/atrasados")
public class AtrasosResource {

    public record Atraso(UUID emprestimoId, String leitor, String email, String tombo, String citacao,
                         LocalDate dataPrevista, long diasDeAtraso) {
    }

    private final CirculacaoQueries circulacao;
    private final Obras obras;

    AtrasosResource(CirculacaoQueries circulacao, Obras obras) {
        this.circulacao = circulacao;
        this.obras = obras;
    }

    @GET
    public List<Atraso> atrasados() {
        var hoje = LocalDate.now();
        var atrasados = circulacao.atrasadosEm(hoje);
        var obrasDosLivros = obras.dosEmprestimos(atrasados);
        return atrasados.stream()
                .map(e -> new Atraso(e.getId(), e.getLeitor().getNome(), e.getLeitor().getEmail(),
                        e.getExemplar().getTombo(), CitacaoAbnt.de(obrasDosLivros.get(e.getExemplar().getLivroId())),
                        e.getDataPrevista(), ChronoUnit.DAYS.between(e.getDataPrevista(), hoje)))
                .toList();
    }
}
