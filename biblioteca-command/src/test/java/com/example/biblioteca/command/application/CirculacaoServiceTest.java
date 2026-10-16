package com.example.biblioteca.command.application;

import com.example.biblioteca.command.domain.Exemplar;
import com.example.biblioteca.command.domain.NaoEncontradoException;
import com.example.biblioteca.command.domain.RegraVioladaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CirculacaoServiceTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    static final ZoneId ZONA = ZoneOffset.UTC;

    final EmMemoria.LivrosEmMemoria livros = new EmMemoria.LivrosEmMemoria();
    final EmMemoria.ExemplaresEmMemoria exemplares = new EmMemoria.ExemplaresEmMemoria();
    final EmMemoria.LeitoresEmMemoria leitores = new EmMemoria.LeitoresEmMemoria();
    final EmMemoria.EmprestimosEmMemoria emprestimos = new EmMemoria.EmprestimosEmMemoria();

    AcervoService acervo;
    UUID leitor;
    UUID livro;

    @BeforeEach
    void preparar() {
        acervo = new AcervoService(livros, exemplares);
        leitor = new LeitorService(leitores).cadastrar("Ana Souza", "ana@example.com");
        livro = acervo.cadastrarLivro(new AcervoService.NovoLivro("9788535910663", "Dom Casmurro", null,
                List.of("Machado de Assis"), 1, "Rio de Janeiro", "Garnier", 1899));
    }

    CirculacaoService noDia(LocalDate dia) {
        var relogio = Clock.fixed(Instant.from(dia.atStartOfDay(ZONA)), ZONA);
        return new CirculacaoService(leitores, exemplares, emprestimos, relogio);
    }

    UUID novoExemplar() {
        return acervo.registrarExemplar(livro, "T-" + UUID.randomUUID());
    }

    @Test
    void emprestarMarcaExemplarComoEmprestado() {
        var exemplar = novoExemplar();
        var id = noDia(HOJE).emprestar(leitor, exemplar);

        assertEquals(Exemplar.Situacao.EMPRESTADO, exemplares.dados.get(exemplar).situacao());
        assertEquals(HOJE.plusDays(14), emprestimos.dados.get(id).dataPrevista());
    }

    @Test
    void exemplarJaEmprestadoNaoPodeSerEmprestadoDeNovo() {
        var exemplar = novoExemplar();
        noDia(HOJE).emprestar(leitor, exemplar);
        var outroLeitor = new LeitorService(leitores).cadastrar("Bruno Lima", "bruno@example.com");

        assertThrows(RegraVioladaException.class, () -> noDia(HOJE).emprestar(outroLeitor, exemplar));
    }

    @Test
    void quartoEmprestimoEhRecusado() {
        for (int i = 0; i < 3; i++) {
            noDia(HOJE).emprestar(leitor, novoExemplar());
        }
        var erro = assertThrows(RegraVioladaException.class, () -> noDia(HOJE).emprestar(leitor, novoExemplar()));
        assertEquals("Limite de 3 empréstimos abertos atingido", erro.getMessage());
    }

    @Test
    void devolucaoAtrasadaSuspendeLeitorUmDiaPorDiaDeAtraso() {
        var exemplar = novoExemplar();
        var id = noDia(HOJE).emprestar(leitor, exemplar);

        var diaDaDevolucao = HOJE.plusDays(14 + 3);
        noDia(diaDaDevolucao).devolver(id);

        assertEquals(Exemplar.Situacao.DISPONIVEL, exemplares.dados.get(exemplar).situacao());
        assertEquals(diaDaDevolucao.plusDays(3), leitores.dados.get(leitor).suspensoAte());
        var erro = assertThrows(RegraVioladaException.class,
                () -> noDia(diaDaDevolucao.plusDays(1)).emprestar(leitor, exemplar));
        assertEquals("Leitor suspenso até " + diaDaDevolucao.plusDays(3), erro.getMessage());
    }

    @Test
    void devolucaoNoPrazoNaoSuspende() {
        var id = noDia(HOJE).emprestar(leitor, novoExemplar());
        noDia(HOJE.plusDays(14)).devolver(id);
        assertEquals(null, leitores.dados.get(leitor).suspensoAte());
    }

    @Test
    void exemplarEmprestadoNaoPodeSerBaixado() {
        var exemplar = novoExemplar();
        noDia(HOJE).emprestar(leitor, exemplar);
        assertThrows(RegraVioladaException.class, () -> acervo.baixarExemplar(exemplar));
    }

    @Test
    void leitorInexistente() {
        assertThrows(NaoEncontradoException.class, () -> noDia(HOJE).emprestar(UUID.randomUUID(), novoExemplar()));
    }
}
