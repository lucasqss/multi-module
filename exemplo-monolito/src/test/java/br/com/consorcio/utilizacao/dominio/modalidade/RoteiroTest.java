package br.com.consorcio.utilizacao.dominio.modalidade;

import br.com.consorcio.utilizacao.dominio.modalidade.imovel.aplicacao.RoteiroImovelUrbanoPronto;
import br.com.consorcio.utilizacao.dominio.modalidade.servico.aplicacao.RoteiroServico;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Modalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regras que todo roteiro precisa cumprir. Os plug-ins são descobertos varrendo o classpath:
 * uma modalidade nova entra na verificação sem ninguém lembrar de registrá-la aqui.
 */
class RoteiroTest {

    static Stream<Roteiro> roteiros() {
        return new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.consorcio.utilizacao.dominio.modalidade").stream()
                .filter(c -> c.isAssignableTo(Modalidade.class) && !c.isInterface() && !c.getModifiers().contains(JavaModifier.ABSTRACT))
                .map(RoteiroTest::instanciar)
                .flatMap(m -> m.roteiros().stream());
    }

    @Test
    void encontraTodasAsModalidades() {
        assertTrue(roteiros().map(Roteiro::codigo).toList().containsAll(List.of("IMOVEL_URBANO_PRONTO", "SERVICO")));
    }

    @ParameterizedTest
    @MethodSource("roteiros")
    void dependenciasApontamParaFasesAnterioresDoProprioRoteiro(Roteiro r) {
        Set<Fase> vistas = EnumSet.noneOf(Fase.class);
        for (FaseRoteiro f : r.fases()) {
            assertTrue(vistas.containsAll(f.dependeDe()),
                    r.codigo() + ": " + f.fase() + " depende de fase ausente ou posterior " + f.dependeDe());
            assertTrue(vistas.add(f.fase()), r.codigo() + ": fase repetida " + f.fase());
        }
    }

    @ParameterizedTest
    @MethodSource("roteiros")
    void portoesPercorremOEstadoMacroEmOrdemValida(Roteiro r) {
        EstadoProcesso atual = EstadoProcesso.RASCUNHO;
        for (FaseRoteiro f : r.fases()) {
            if (f.estadoAoConcluir().isPresent()) {
                EstadoProcesso proximo = f.estadoAoConcluir().get();
                // EM_PAGAMENTO é aplicado pelo módulo pagamento, entre APTO_PAGAMENTO e PAGO.
                if (atual == EstadoProcesso.APTO_PAGAMENTO && proximo == EstadoProcesso.PAGO) {
                    atual = EstadoProcesso.EM_PAGAMENTO;
                }
                assertTrue(atual.proximos().contains(proximo), r.codigo() + ": " + atual + " -> " + proximo + " não é permitido");
                atual = proximo;
            }
        }
        assertEquals(EstadoProcesso.CONCLUIDO, atual, r.codigo() + " não termina em CONCLUIDO");
    }

    @Test
    void servicoNaoTemAlienacaoDoObjeto() {
        List<Fase> fases = new RoteiroServico().fases().stream().map(FaseRoteiro::fase).toList();
        assertFalse(fases.contains(Fase.CONSTITUICAO_GARANTIA));
        assertFalse(fases.contains(Fase.VISTORIA_AVALIACAO));
    }

    @Test
    void imovelPagaSoDepoisDaConferenciaDoRegistro() {
        FaseRoteiro liberacao = new RoteiroImovelUrbanoPronto().fase(Fase.LIBERACAO_PAGAMENTO).orElseThrow();
        assertTrue(liberacao.dependeDe().contains(Fase.CONFERENCIA_GARANTIA));
        assertTrue(liberacao.automatica());
    }

    private static Modalidade instanciar(JavaClass c) {
        try {
            return (Modalidade) c.reflect().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(c.getName() + " precisa de construtor sem argumentos", e);
        }
    }
}
