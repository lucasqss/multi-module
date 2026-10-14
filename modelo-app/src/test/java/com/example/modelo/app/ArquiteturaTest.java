package com.example.modelo.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

/**
 * Regras de acoplamento entre módulos que o Maven sozinho não garante. Ficam no modelo-app porque é o único
 * módulo que tem todos os outros no classpath. Valem para qualquer módulo novo, desde que siga a convenção de
 * pacote raiz {@code com.example.modelo.<módulo>}.
 *
 * <p>Usa a API core do ArchUnit ({@code check}) dentro de um {@code @Test} comum: o engine
 * {@code archunit-junit5} não executa sob o JUnit 6 do BOM do Quarkus (rodaria 0 testes).
 */
class ArquiteturaTest {

    private static final String RAIZ = "com.example.modelo";

    /** Classes de produção de todos os módulos (as de teste ficam de fora). */
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(local -> !local.contains("/test-classes/"))
            .importPackages(RAIZ);

    @Test
    void pacotesSemCiclos() {
        // Cada pacote é uma fatia: nenhum ciclo entre módulos nem entre pacotes de um mesmo módulo
        slices().matching(RAIZ + ".(**)").should().beFreeOfCycles().check(CLASSES);
    }

    @Test
    void pacotesInternoSoSaoUsadosPeloProprioModulo() {
        classes().that().resideInAPackage(RAIZ + "..")
                .should(naoUsarPacoteInternoDeOutroModulo())
                .check(CLASSES);
    }

    @Test
    void soOAppConheceRestEJson() {
        // Detalhes de entrega ficam no modelo-app; as bibliotecas expõem só tipos próprios
        noClasses().that().resideInAPackage(RAIZ + "..").and().resideOutsideOfPackage(RAIZ + ".app..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "com.fasterxml.jackson..")
                .check(CLASSES);
    }

    private static ArchCondition<JavaClass> naoUsarPacoteInternoDeOutroModulo() {
        return new ArchCondition<>("não usar pacotes 'interno' de outro módulo") {
            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    var alvo = dependencia.getTargetClass();
                    if (ehInterno(alvo) && !modulo(alvo).equals(modulo(origem))) {
                        eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
                    }
                }
            }
        };
    }

    private static boolean ehInterno(JavaClass classe) {
        return classe.getPackageName().startsWith(RAIZ + ".") && ("." + classe.getPackageName() + ".").contains(".interno.");
    }

    /** {@code com.example.modelo.core.interno} → {@code core}. */
    private static String modulo(JavaClass classe) {
        var pacote = classe.getPackageName();
        if (!pacote.startsWith(RAIZ + ".")) {
            return "";
        }
        var resto = pacote.substring(RAIZ.length() + 1);
        var ponto = resto.indexOf('.');
        return ponto < 0 ? resto : resto.substring(0, ponto);
    }
}
