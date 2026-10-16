package com.example.biblioteca;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Regras de acoplamento ENTRE módulos que o Maven sozinho não garante. Ficam no biblioteca-app porque é o único
 * módulo que tem todos os outros no classpath (e tudo vai para o mesmo jar). Regras internas de um módulo ficam
 * nele, como as camadas do command em {@code com.example.biblioteca.command.ArquiteturaTest}.
 *
 * <p>Usa a API core do ArchUnit ({@code check}) dentro de um {@code @Test} comum: o engine
 * {@code archunit-junit5} não executa sob o JUnit 6 do BOM do Quarkus (rodaria 0 testes).
 */
class ArquiteturaModulosTest {

    /**
     * Pacote raiz de cada módulo. Módulo novo entra aqui: a regra {@link #todaClassePertenceAUmModulo()} falha
     * até ele ser registrado.
     */
    private static final Map<String, String> MODULOS = Map.of(
            "jpa-core", "com.example.jpacore",
            "string-generator", "com.example.stringgenerator",
            "biblioteca-command", "com.example.biblioteca.command",
            "biblioteca-query", "com.example.biblioteca.query");

    private static final String[] REST_E_JSON = {
            "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "com.fasterxml.jackson.."};

    /**
     * Classes de produção de todos os módulos. O {@code ImportOption.DO_NOT_INCLUDE_TESTS} do ArchUnit procura
     * {@code target/test-classes} e não reconhece o layout {@code target/<artifactId>/test-classes} deste build.
     */
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(local -> !local.contains("/test-classes/"))
            .importPackages("com.example");

    @Test
    void todaClassePertenceAUmModulo() {
        classes().should(pertencerAUmModuloRegistrado()).check(CLASSES);
    }

    @Test
    void pacotesSemCiclos() {
        // Cada pacote é uma fatia: nenhum ciclo entre módulos nem entre pacotes de um mesmo módulo
        slices().matching("com.example.(**)").should().beFreeOfCycles().check(CLASSES);
    }

    @Test
    void commandEQueryNaoSeEnxergam() {
        // CQRS com independência de design: no jar único as classes convivem, mas um lado não usa o outro
        noClasses().that().resideInAPackage("com.example.biblioteca.command..")
                .should().dependOnClassesThat().resideInAPackage("com.example.biblioteca.query..")
                .check(CLASSES);
        noClasses().that().resideInAPackage("com.example.biblioteca.query..")
                .should().dependOnClassesThat().resideInAPackage("com.example.biblioteca.command..")
                .check(CLASSES);
    }

    @Test
    void stringGeneratorEhJavaPuro() {
        noClasses().that().resideInAPackage("com.example.stringgenerator..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "com.example.biblioteca..")
                .check(CLASSES);
    }

    @Test
    void jpaCoreSoConheceOSchema() {
        noClasses().that().resideInAPackage("com.example.jpacore..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.example.stringgenerator..", "com.example.biblioteca..")
                .check(CLASSES);
    }

    @Test
    void restEJsonSoNasBordas() {
        // Só os adapters REST do command e o query (que é só leitura + REST) falam HTTP/JSON
        noClasses().that().resideInAPackage("com.example..")
                .and().resideOutsideOfPackages("com.example.biblioteca.command.adapter.rest..",
                        "com.example.biblioteca.query..")
                .should().dependOnClassesThat().resideInAnyPackage(REST_E_JSON)
                .check(CLASSES);
    }

    @Test
    void pacotesInternoSoSaoUsadosPeloProprioModulo() {
        // Detalhes que precisam ser public (para outro pacote do próprio módulo) vão em ..interno..
        classes().should(naoUsarPacoteInternoDeOutroModulo()).check(CLASSES);
    }

    private static Optional<String> modulo(JavaClass classe) {
        var pacote = classe.getPackageName();
        return MODULOS.entrySet().stream()
                .filter(m -> pacote.equals(m.getValue()) || pacote.startsWith(m.getValue() + "."))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private static ArchCondition<JavaClass> pertencerAUmModuloRegistrado() {
        return new ArchCondition<>("pertencer a um módulo registrado em MODULOS") {
            @Override
            public void check(JavaClass classe, ConditionEvents eventos) {
                if (modulo(classe).isEmpty()) {
                    eventos.add(SimpleConditionEvent.violated(classe, classe.getName()
                            + " não pertence a nenhum módulo: registre o pacote raiz do módulo em MODULOS"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> naoUsarPacoteInternoDeOutroModulo() {
        return new ArchCondition<>("não usar pacotes 'interno' de outro módulo") {
            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    var alvo = dependencia.getTargetClass();
                    var interno = ("." + alvo.getPackageName() + ".").contains(".interno.");
                    if (interno && modulo(alvo).isPresent() && !modulo(alvo).equals(modulo(origem))) {
                        eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
                    }
                }
            }
        };
    }
}
