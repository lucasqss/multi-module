package com.example.processo;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * As camadas e quem pode usar quem. Com um módulo só, o compilador não impede que uma camada use outra; estas regras
 * fazem esse papel e quebram o build (ADR 0014).
 *
 * <pre>
 * apresentacao  →  negocio  →  integracao
 *      │              └──────→  persistencia.repositorio
 *      └──────────────────────→  persistencia.consulta   (leitura direta, sem serviço que só repassa)
 * modelo: usado por todas; não usa nenhuma
 * </pre>
 *
 * <p>Usa a API core do ArchUnit ({@code check}) dentro de um {@code @Test} comum: o engine {@code archunit-junit5}
 * não executa sob o JUnit 6 do BOM do Quarkus (rodaria 0 testes).
 */
class ArquiteturaTest {

    private static final String BASE = "com.example.processo";
    private static final String APRESENTACAO = BASE + ".apresentacao..";
    private static final String NEGOCIO = BASE + ".negocio..";
    private static final String INTEGRACAO = BASE + ".integracao..";
    private static final String PERSISTENCIA = BASE + ".persistencia..";
    private static final String MODELO = BASE + ".modelo..";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);

    @Test
    void camadas() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Apresentacao").definedBy(APRESENTACAO)
                .layer("Negocio").definedBy(NEGOCIO)
                .layer("Integracao").definedBy(INTEGRACAO)
                .layer("Persistencia").definedBy(PERSISTENCIA)
                .layer("Modelo").definedBy(MODELO)
                .whereLayer("Apresentacao").mayNotBeAccessedByAnyLayer()
                .whereLayer("Negocio").mayOnlyBeAccessedByLayers("Apresentacao")
                .whereLayer("Integracao").mayOnlyBeAccessedByLayers("Negocio")
                .whereLayer("Persistencia").mayOnlyBeAccessedByLayers("Negocio", "Apresentacao")
                .whereLayer("Modelo").mayOnlyBeAccessedByLayers("Apresentacao", "Negocio", "Integracao", "Persistencia")
                .check(CLASSES);
    }

    @Test
    void todaClasseEstaNumaCamada() {
        classes().should().resideInAnyPackage(APRESENTACAO, NEGOCIO, INTEGRACAO, PERSISTENCIA, MODELO).check(CLASSES);
    }

    @Test
    void apresentacaoSoLeConsultasDaPersistencia() {
        // Gravar passa sempre pelo negócio (regras, transação); ler pode ir direto às projeções
        noClasses().that().resideInAPackage(APRESENTACAO)
                .should().dependOnClassesThat().resideInAPackage(BASE + ".persistencia.repositorio..")
                .check(CLASSES);
    }

    @Test
    void modelosSemDependenciaDeFramework() {
        // O modelo conhece só JPA (as anotações do mapeamento): nada de REST, CDI, fila ou Quarkus
        noClasses().that().resideInAPackage(MODELO)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "jakarta.enterprise..", "jakarta.inject..", "io.quarkus..", "io.smallrye..",
                        "org.eclipse.microprofile..")
                .check(CLASSES);
    }

    @Test
    void httpSoNaApresentacaoENaIntegracao() {
        noClasses().that().resideOutsideOfPackages(APRESENTACAO, INTEGRACAO)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "org.eclipse.microprofile.rest..")
                .check(CLASSES);
    }

    @Test
    void modalidadesNaoSeEnxergam() {
        // Cada pacote do negócio (imobiliario, veicular, agencia) só usa o motor (negocio.processo), nunca outro
        // pacote; e o motor não conhece nenhuma modalidade
        slices().matching(BASE + ".negocio.(*)..").should().notDependOnEachOther()
                .ignoreDependency(alwaysTrue(), resideInAPackage(BASE + ".negocio.processo.."))
                .check(CLASSES);
    }

    @Test
    void sistemasExternosNaoSeEnxergam() {
        slices().matching(BASE + ".integracao.(*)..").should().notDependOnEachOther().check(CLASSES);
    }

    @Test
    void canaisDeEntradaNaoSeEnxergam() {
        // A fila não reaproveita o corpo do REST, e vice-versa
        slices().matching(BASE + ".apresentacao.(*)..").should().notDependOnEachOther().check(CLASSES);
    }

    @Test
    void semCiclos() {
        slices().matching(BASE + ".(**)").should().beFreeOfCycles().check(CLASSES);
    }
}
