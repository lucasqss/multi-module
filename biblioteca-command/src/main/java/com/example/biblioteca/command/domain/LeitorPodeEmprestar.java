package com.example.biblioteca.command.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regra que atravessa agregados: depende do {@link Leitor} e dos {@link Emprestimo}s abertos dele.
 * Quem monta a {@link Situacao} é o caso de uso, usando o port {@link Emprestimos#abertosDo}.
 */
public class LeitorPodeEmprestar implements Especificacao<LeitorPodeEmprestar.Situacao> {

    public static final int MAX_EMPRESTIMOS_ABERTOS = 3;

    public record Situacao(Leitor leitor, List<Emprestimo> abertos, LocalDate hoje) {
    }

    private static final Especificacao<Situacao> NAO_SUSPENSO = s -> s.leitor().suspensoEm(s.hoje())
            ? Optional.of("Leitor suspenso até " + s.leitor().suspensoAte())
            : Optional.empty();

    private static final Especificacao<Situacao> SEM_ATRASO = s -> s.abertos().stream().anyMatch(e -> e.atrasadoEm(s.hoje()))
            ? Optional.of("Leitor possui empréstimo atrasado")
            : Optional.empty();

    private static final Especificacao<Situacao> ABAIXO_DO_LIMITE = s -> s.abertos().size() < MAX_EMPRESTIMOS_ABERTOS
            ? Optional.empty()
            : Optional.of("Limite de " + MAX_EMPRESTIMOS_ABERTOS + " empréstimos abertos atingido");

    /**
     * A ordem define o motivo exibido quando várias regras falham: primeiro o impedimento que o leitor não
     * resolve sozinho (suspensão), depois o que ele resolve devolvendo um livro — atraso antes do limite,
     * porque devolver o atrasado resolve os dois.
     */
    private static final Especificacao<Situacao> REGRA = NAO_SUSPENSO.e(SEM_ATRASO).e(ABAIXO_DO_LIMITE);

    @Override
    public Optional<String> violacao(Situacao situacao) {
        return REGRA.violacao(situacao);
    }
}
