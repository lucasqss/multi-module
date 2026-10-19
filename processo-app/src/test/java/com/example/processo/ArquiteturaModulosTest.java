package com.example.processo;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
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
 * Regras de acoplamento ENTRE módulos que o Maven sozinho não garante. Ficam no processo-app porque é o único
 * módulo que tem todos os outros no classpath. Regras internas de um módulo ficam nele (motor x segmentos e
 * camadas, no {@code com.example.processo.core.ArquiteturaTest}).
 *
 * <p>Usa a API core do ArchUnit ({@code check}) dentro de um {@code @Test} comum: o engine
 * {@code archunit-junit5} não executa sob o JUnit 6 do BOM do Quarkus (rodaria 0 testes).
 */
class ArquiteturaModulosTest {

    private static final String CORE = "com.example.processo.core";
    private static final String QUERY = "com.example.processo.query";
    private static final String IMPRESSAO = "com.example.processo.impressao";
    private static final String INTEGRACOES = "com.example.processo.integracoes";
    private static final String APP = "com.example.processo.app";

    /**
     * Pacote raiz de cada módulo. Módulo novo entra aqui: a regra {@link #todaClassePertenceAUmModulo()} falha
     * até ele ser registrado.
     */
    private static final Map<String, String> MODULOS = Map.of(
            "jpa-core", "com.example.jpacore",
            "processo-core", CORE,
            "processo-query", QUERY,
            "integracoes", INTEGRACOES,
            "impressao", IMPRESSAO,
            "processo-app", APP);

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example");

    @Test
    void todaClassePertenceAUmModulo() {
        classes().should(pertencerAUmModuloRegistrado()).check(CLASSES);
    }

    @Test
    void pacotesSemCiclos() {
        slices().matching("com.example.(**)").should().beFreeOfCycles().check(CLASSES);
    }

    @Test
    void escritaELeituraNaoSeEnxergam() {
        // CQRS: no jar único as classes convivem, mas o core não usa consultas e as consultas não usam o domínio.
        // Os dois lados só se encontram nos endpoints do processo-app.
        noClasses().that().resideInAPackage(CORE + "..")
                .should().dependOnClassesThat().resideInAPackage(QUERY + "..")
                .check(CLASSES);
        noClasses().that().resideInAPackage(QUERY + "..")
                .should().dependOnClassesThat().resideInAPackage(CORE + "..")
                .check(CLASSES);
    }

    @Test
    void ninguemDependeDaImpressao() {
        // Descartável: apagar o módulo não pode quebrar a compilação de nenhum outro
        noClasses().that().resideOutsideOfPackage(IMPRESSAO + "..")
                .should().dependOnClassesThat().resideInAPackage(IMPRESSAO + "..")
                .check(CLASSES);
    }

    @Test
    void integracoesSoImplementamPortsDoCore() {
        // Ninguém chama a integração diretamente: o core define o port e a integração o implementa (o CDI liga).
        noClasses().that().resideOutsideOfPackage(INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAPackage(INTEGRACOES + "..")
                .check(CLASSES);
        // Do core, a integração só enxerga os domains (ports e tipos), nunca casos de uso nem adapters
        noClasses().that().resideInAPackage(INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAnyPackage(CORE + "..application..", CORE + "..adapter..")
                .check(CLASSES);
        // Um sistema externo por pacote, sem um enxergar o outro
        slices().matching(INTEGRACOES + ".(*)..").should().notDependOnEachOther().check(CLASSES);
    }

    @Test
    void ninguemDependeDoApp() {
        noClasses().that().resideOutsideOfPackage(APP + "..")
                .should().dependOnClassesThat().resideInAPackage(APP + "..")
                .check(CLASSES);
    }

    @Test
    void jpaCoreSoConheceOSchema() {
        noClasses().that().resideInAPackage("com.example.jpacore..")
                .should().dependOnClassesThat().resideInAPackage("com.example.processo..")
                .check(CLASSES);
    }

    @Test
    void httpSoNasBordas() {
        // REST de entrada só no app; impressão e integrações são clientes HTTP de sistemas externos
        noClasses().that().resideInAPackage("com.example..")
                .and().resideOutsideOfPackages(APP + "..", IMPRESSAO + "..", INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "org.eclipse.microprofile.rest..")
                .check(CLASSES);
    }

    @Test
    void appNaoUsaEntidadesNemRepositoriosDeEscrita() {
        // O app traduz HTTP: cria pelo motor e lê pelas projeções do query. Entidades JPA e adapters do core ficam
        // por trás (enums do jpa-core, usados nas projeções, são permitidos).
        noClasses().that().resideInAPackage(APP + "..")
                .should().dependOnClassesThat().areAnnotatedWith(jakarta.persistence.Entity.class)
                .orShould().dependOnClassesThat().resideInAPackage(CORE + "..adapter..")
                .check(CLASSES);
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
}
