package com.example.processo;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Regras de acoplamento ENTRE módulos que o Maven sozinho não garante. Ficam no processo-app porque é o único
 * módulo que tem todos os outros no classpath. Regras internas de um módulo ficam nele (camadas do motor e do
 * segmento).
 *
 * <p>Usa a API core do ArchUnit ({@code check}) dentro de um {@code @Test} comum: o engine
 * {@code archunit-junit5} não executa sob o JUnit 6 do BOM do Quarkus (rodaria 0 testes).
 */
class ArquiteturaModulosTest {

    private static final String MOTOR = "com.example.processo.motor";
    private static final String IMPRESSAO = "com.example.processo.impressao";

    /** Um pacote raiz por segmento. Segmento novo entra aqui e no {@link #MODULOS}. */
    private static final List<String> SEGMENTOS = List.of("com.example.processo.imobiliario");

    /**
     * Pacote raiz de cada módulo. Módulo novo entra aqui: a regra {@link #todaClassePertenceAUmModulo()} falha
     * até ele ser registrado.
     */
    private static final Map<String, String> MODULOS = Map.of(
            "jpa-core", "com.example.jpacore",
            "processo-motor", MOTOR,
            "segmento-imobiliario", "com.example.processo.imobiliario",
            "impressao", IMPRESSAO);

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
    void motorNaoConheceSegmentosNemImpressao() {
        // O motor é único: segmento novo ou impressão removida não podem exigir mudança nele
        noClasses().that().resideInAPackage(MOTOR + "..")
                .should().dependOnClassesThat().resideInAnyPackage(pacotes(SEGMENTOS, IMPRESSAO))
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
    void segmentosNaoSeEnxergam() {
        for (var segmento : SEGMENTOS) {
            var outros = SEGMENTOS.stream().filter(s -> !s.equals(segmento)).toList();
            if (!outros.isEmpty()) {
                noClasses().that().resideInAPackage(segmento + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(pacotes(outros))
                        .check(CLASSES);
            }
        }
    }

    @Test
    void jpaCoreSoConheceOSchema() {
        noClasses().that().resideInAPackage("com.example.jpacore..")
                .should().dependOnClassesThat().resideInAPackage("com.example.processo..")
                .check(CLASSES);
    }

    @Test
    void restSoNasBordas() {
        // Só os adapters REST e o cliente do sistema de PDF falam HTTP
        noClasses().that().resideInAPackage("com.example..")
                .and().resideOutsideOfPackages("..adapter.rest..", IMPRESSAO + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "org.eclipse.microprofile.rest..")
                .check(CLASSES);
    }

    private static String[] pacotes(List<String> raizes, String... outras) {
        return java.util.stream.Stream.concat(raizes.stream(), java.util.Arrays.stream(outras))
                .map(raiz -> raiz + "..")
                .toArray(String[]::new);
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
