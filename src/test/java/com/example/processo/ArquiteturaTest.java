package com.example.processo;

import com.example.processo.negocio.processo.Modalidade;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.persistence.Entity;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * As fitness functions da arquitetura: as camadas e quem pode usar quem, onde o JPA pode aparecer, onde fica a
 * transação e os nomes pelo papel. Com um módulo só, o compilador não impede nada disso; estas regras fazem esse
 * papel e quebram o build (ADR 0014).
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
    void jpaSoNoModeloENaPersistencia() {
        // A entidade é o modelo (ADR 0014), mas o mapeamento e o acesso ao banco não passam disso: negócio, integração
        // e apresentação usam as entidades pelos getters e métodos de evento, sem API do JPA ou do Hibernate
        noClasses().that().resideOutsideOfPackages(MODELO, PERSISTENCIA)
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
                .check(CLASSES);
    }

    @Test
    void entidadesNoModeloOuNaPersistenciaERepositoriesNaPersistencia() {
        // Entidade de negócio no modelo; tabela só técnica (ex.: ChaveIdempotencia) na persistência
        classes().that().areAnnotatedWith(Entity.class).should().resideInAnyPackage(MODELO, PERSISTENCIA).check(CLASSES);
        classes().that().areAnnotatedWith(Repository.class).should().resideInAPackage(PERSISTENCIA).check(CLASSES);
    }

    @Test
    void apiNaoDevolveEntidade() {
        // O contrato da API não muda quando o schema muda: resource devolve response ou projeção, nem List<Entidade>
        methods().that().areDeclaredInClassesThat().areAnnotatedWith(Path.class)
                .and().areDeclaredInClassesThat().areNotInterfaces().and().arePublic()
                .should(naoDevolverEntidade())
                .check(CLASSES);
    }

    @Test
    void transacaoSoNoNegocio() {
        // Quem decide o tamanho da transação é o negócio: QuarkusTransaction no motor, @Transactional nos cadastros
        noClasses().that().resideOutsideOfPackage(NEGOCIO)
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.transaction..", "io.quarkus.narayana..")
                .check(CLASSES);
    }

    @Test
    void nomesPeloPapel() {
        // @Path em classe é endpoint de entrada; em interface com @RegisterRestClient, cliente de sistema externo
        classes().that().areAnnotatedWith(Path.class).and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Resource").andShould().resideInAPackage(BASE + ".apresentacao.rest..")
                .check(CLASSES);
        classes().that().areAnnotatedWith(RegisterRestClient.class)
                .should().haveSimpleNameEndingWith("Client").andShould().resideInAPackage(INTEGRACAO)
                .check(CLASSES);
        // O que o negócio chama em cada sistema externo; as simulações do dev (@Alternative) ficam de fora
        classes().that().resideInAPackage(INTEGRACAO).and().areAnnotatedWith(ApplicationScoped.class)
                .and().areNotAnnotatedWith(Alternative.class)
                .should().haveSimpleNameEndingWith("Integracao")
                .check(CLASSES);
        classes().that().implement(Modalidade.class)
                .should().haveSimpleNameStartingWith("Modalidade").andShould().resideInAPackage(NEGOCIO)
                .check(CLASSES);
        classes().that().resideInAPackage(NEGOCIO).and().areAnnotatedWith(ApplicationScoped.class)
                .and().doNotImplement(Modalidade.class)
                .should().haveSimpleNameEndingWith("Service")
                .check(CLASSES);
    }


    @Test
    void testesPeloNome() {
        // @QuarkusTest sobe o Oracle: o nome *BancoTest é o que o tira do mvn test -Prapido (o filtro por tag não
        // serve, porque o Quarkus sobe o Dev Services já na descoberta). @QuarkusIntegrationTest roda no failsafe: *IT
        var testes = new ClassFileImporter().withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages(BASE);
        classes().that().areAnnotatedWith(QuarkusTest.class).should().haveSimpleNameEndingWith("BancoTest").check(testes);
        classes().that().areAnnotatedWith(QuarkusIntegrationTest.class).should().haveSimpleNameEndingWith("IT").check(testes);
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

    /** Olha o tipo de retorno inteiro, inclusive os genéricos ({@code List<Agencia>}, {@code Optional<Processo>}). */
    private static ArchCondition<JavaMethod> naoDevolverEntidade() {
        return new ArchCondition<>("não devolver entidade JPA") {
            @Override
            public void check(JavaMethod metodo, ConditionEvents eventos) {
                metodo.getReturnType().getAllInvolvedRawTypes().stream()
                        .filter(tipo -> tipo.isAnnotatedWith(Entity.class))
                        .forEach(entidade -> eventos.add(SimpleConditionEvent.violated(metodo,
                                metodo.getFullName() + " devolve a entidade " + entidade.getSimpleName()
                                        + ": use um response ou uma projeção")));
            }
        };
    }
}
