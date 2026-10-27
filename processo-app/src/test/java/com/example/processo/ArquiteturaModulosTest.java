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
            "processo-app", APP);

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            // O test-jar do core (fakes e contratos) também está no classpath de teste: não é código de produção
            .withImportOption(local -> !local.contains("-tests.jar"))
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
    void integracoesSoImplementamPortsDoCore() {
        // Ninguém chama a integração diretamente: o core define o port e a integração o implementa (o CDI liga).
        noClasses().that().resideOutsideOfPackage(INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAPackage(INTEGRACOES + "..")
                .check(CLASSES);
        // Do core, a integração só enxerga o domínio e os ports de saída que implementa
        noClasses().that().resideInAPackage(INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        CORE + "..application.service..", CORE + "..application.port.spi..",
                        CORE + "..adapter..", CORE + "..config..")
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
        // REST de entrada só no app; as integrações são clientes HTTP de sistemas externos
        noClasses().that().resideInAPackage("com.example..")
                .and().resideOutsideOfPackages(APP + "..", INTEGRACOES + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "org.eclipse.microprofile.rest..")
                .check(CLASSES);
    }

    @Test
    void appEntraNoCorePorServicos() {
        // As entradas chamam serviços do core (o motor, ou o serviço de um cadastro simples) e usam os tipos do
        // domínio. Ports de saída, SPI e adapters ficam por trás. Entidades JPA também: a leitura usa as projeções do
        // query ou os records do cadastro (enums do jpa-core são permitidos).
        noClasses().that().resideInAPackage(APP + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        CORE + "..application.port.out..", CORE + "..application.port.spi..",
                        CORE + "..adapter..", CORE + "..config..")
                .orShould().dependOnClassesThat().areAnnotatedWith(jakarta.persistence.Entity.class)
                .check(CLASSES);
    }

    @Test
    void entradasOrganizadasPorCanalEPapel() {
        // Cada canal de entrada (rest, messaging, ...) tem seu dto; o dto converte de/para o domínio, mas não conhece
        // quem o recebe (resource, consumer)
        noClasses().that().resideInAPackage(APP + ".adapter.inbound.(*).dto..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        APP + ".adapter.inbound.(*).resource..", APP + ".adapter.inbound.(*).consumer..")
                .check(CLASSES);
        // Um canal não usa as classes de outro: a fila não reaproveita o corpo do REST, e vice-versa
        slices().matching(APP + ".adapter.inbound.(*)..").should().notDependOnEachOther().check(CLASSES);
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
