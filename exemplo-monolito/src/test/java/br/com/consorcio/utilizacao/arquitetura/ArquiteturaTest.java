package br.com.consorcio.utilizacao.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Fitness functions do padrão: rest / dominio / integracao / compartilhado, com o domínio particionado
 * por capacidade. Quebrar uma regra quebra o build, não a revisão de código.
 */
class ArquiteturaTest {

    static final String BASE = "br.com.consorcio.utilizacao";
    static final String DOMINIO = BASE + ".dominio";
    static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    // ---------------------------------------------------------------- camadas de cima

    @Test
    void restSoFalaComAsApis() {
        noClasses().that().resideInAPackage(BASE + ".rest..")
                .should().dependOnClassesThat().resideInAnyPackage(DOMINIO + "..modelo..", DOMINIO + "..aplicacao..",
                        DOMINIO + "..plugin..", BASE + ".integracao..")
                .check(classes);
    }

    @Test
    void ninguemDependeDoRest() {
        noClasses().that().resideOutsideOfPackage(BASE + ".rest..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".rest..")
                .check(classes);
    }

    @Test
    void dominioNaoConheceIntegracaoNemTecnologiaDeAdaptador() {
        noClasses().that().resideInAPackage(DOMINIO + "..")
                .should().dependOnClassesThat().resideInAnyPackage(BASE + ".integracao..",
                        "io.agroal..", "org.eclipse.microprofile.rest.client..", "jakarta.ws.rs..")
                .check(classes);
    }

    @Test
    void compartilhadoNaoConheceNinguem() {
        noClasses().that().resideInAPackage(BASE + ".compartilhado..")
                .should().dependOnClassesThat().resideInAnyPackage(DOMINIO + "..", BASE + ".integracao..", BASE + ".rest..")
                .check(classes);
    }

    // ---------------------------------------------------------------- fronteiras entre módulos

    /** Fora de um módulo só se usa a api dele. Exceção explícita: plug-ins estendem o núcleo do processo. */
    @Test
    void entreModulosSoPelaApi() {
        for (String modulo : new String[]{"nucleo.partes", "nucleo.credito", "nucleo.formalizacao", "nucleo.documento",
                "nucleo.pagamento", "garantia"}) {
            String pacote = DOMINIO + "." + modulo;
            noClasses().that().resideInAPackage(DOMINIO + "..").and().resideOutsideOfPackage(pacote + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(pacote + ".modelo..", pacote + ".aplicacao..")
                    .as("fora de " + modulo + " só se usa " + modulo + ".api")
                    .check(classes);
        }
        noClasses().that().resideInAPackage(DOMINIO + "..").and().resideOutsideOfPackage(DOMINIO + ".nucleo.processo..")
                .and().resideOutsideOfPackage(DOMINIO + ".modalidade..")
                .should().dependOnClassesThat().resideInAnyPackage(DOMINIO + ".nucleo.processo.modelo..",
                        DOMINIO + ".nucleo.processo.aplicacao..", DOMINIO + ".nucleo.processo.plugin..")
                .check(classes);
    }

    @Test
    void pluginsUsamOContratoENaoAAplicacaoDoNucleo() {
        noClasses().that().resideInAPackage(DOMINIO + ".modalidade..")
                .should().dependOnClassesThat().resideInAPackage(DOMINIO + ".nucleo.processo.aplicacao..")
                .check(classes);
    }

    @Test
    void nucleoNaoConheceAsModalidades() {
        noClasses().that().resideInAPackage(DOMINIO + ".nucleo..")
                .should().dependOnClassesThat().resideInAPackage(DOMINIO + ".modalidade..")
                .check(classes);
    }

    @Test
    void modalidadesNaoSeConhecem() {
        slices().matching(DOMINIO + ".modalidade.(*)..").should().notDependOnEachOther().check(classes);
    }

    @Test
    void garantiaNaoDependeDoProcesso() {
        noClasses().that().resideInAPackage(DOMINIO + ".garantia..")
                .should().dependOnClassesThat().resideInAnyPackage(DOMINIO + ".nucleo..", DOMINIO + ".modalidade..")
                .check(classes);
    }

    @Test
    void modulosSemCiclos() {
        slices().matching(DOMINIO + ".(*.*)..").should().beFreeOfCycles().check(classes);
        slices().matching(BASE + ".(*)..").should().beFreeOfCycles().check(classes);
    }

    // ---------------------------------------------------------------- dentro do módulo

    @Test
    void entidadesFicamNoModelo() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAnyPackage(DOMINIO + "..modelo..", BASE + ".integracao.outbox..")
                .check(classes);
    }

    @Test
    void modeloNaoDependeDaAplicacaoNemDaApiDeOutros() {
        noClasses().that().resideInAPackage(DOMINIO + "..modelo..")
                .should().dependOnClassesThat().resideInAPackage(DOMINIO + "..aplicacao..")
                .check(classes);
    }
}
