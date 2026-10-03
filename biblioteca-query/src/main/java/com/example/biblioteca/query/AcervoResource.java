package com.example.biblioteca.query;

import com.example.stringgenerator.ReferenciaAbnt;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Path("/acervo")
public class AcervoResource {

    public record ItemAcervo(UUID livroId, String isbn, String titulo, String referencia, long disponiveis) {
    }

    private final AcervoQueries acervo;

    AcervoResource(AcervoQueries acervo) {
        this.acervo = acervo;
    }

    @GET
    public List<ItemAcervo> buscar(@QueryParam("titulo") String titulo, @QueryParam("autor") String autor) {
        var autoriasPorLivro = Obras.porLivro(acervo.autorias(titulo, autor));
        if (autoriasPorLivro.isEmpty()) {
            return List.of();
        }
        Map<UUID, Long> disponiveis = acervo.disponiveisDos(autoriasPorLivro.keySet()).stream()
                .collect(toMap(Disponiveis::livroId, Disponiveis::quantidade));
        return autoriasPorLivro.entrySet().stream()
                .map(e -> {
                    var livro = e.getValue().getFirst().getLivro();
                    return new ItemAcervo(e.getKey(), livro.getIsbn(), livro.getTitulo(),
                            ReferenciaAbnt.de(Obras.obra(e.getValue())), disponiveis.getOrDefault(e.getKey(), 0L));
                })
                .toList();
    }
}
