package TROQUE.PACOTE.BASE;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.persistence.Entity;
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
 * As fitness functions do modelo em camadas (kit-camadas, GUIA.md seção 2): quem pode usar quem, onde o JPA e a
 * transação podem aparecer e os nomes pelo papel. Num módulo só o compilador não impede nada disso; estas regras
 * fazem esse papel e quebram o build.
 *
 * <pre>
 * apresentacao  →  negocio  →  integracao
 *      │              └──────→  persistencia.repositorio
 *      └──────────────────────→  persistencia.consulta   (leitura direta, sem serviço que só repassa)
 * modelo: usado por todas; não usa nenhuma
 * </pre>
 *
 * <p><b>Para instalar:</b> troque o {@code package} acima e {@link #BASE}; se o projeto tiver um ponto de extensão
 * com várias implementações (no modelo de referência, {@code Modalidade}), ponha o nome completo em
 * {@link #PONTO_DE_EXTENSAO}. Requer {@code com.tngtech.archunit:archunit} (não o {@code archunit-junit5}: o engine
 * dele não roda sob o JUnit 6 do BOM do Quarkus, por isso as regras usam {@code check} dentro de {@code @Test}).
 *
 * <p><b>Modo migração</b> ({@link #MIGRACAO} = true): cada regra é "congelada" com {@link FreezingArchRule}. Na
 * primeira execução as violações que já existem são gravadas em {@code src/test/resources/archunit_store} (commit
 * esse diretório); daí em diante só violação <i>nova</i> quebra o build, e as que forem corrigidas saem do arquivo
 * sozinhas. Precisa de {@code src/test/resources/archunit.properties} com:
 * <pre>
 * freeze.store.default.path=src/test/resources/archunit_store
 * freeze.store.default.allowStoreCreation=true
 * </pre>
 * A migração termina com {@code MIGRACAO = false}, o diretório {@code archunit_store} apagado e tudo verde.
 */
class ArquiteturaTest {

    /** Pacote raiz do projeto. TROQUE. */
    private static final String BASE = "TROQUE.PACOTE.BASE";

    /** Nome completo da interface do ponto de extensão do negócio (ex.: Modalidade), ou null se não houver. */
    private static final String PONTO_DE_EXTENSAO = null;

    /** true durante a migração: congela as violações existentes e barra só as novas. */
    private static final boolean MIGRACAO = false;

    private static final String APRESENTACAO = BASE + ".apresentacao..";
    private static final String NEGOCIO = BASE + ".negocio..";
    private static final String INTEGRACAO = BASE + ".integracao..";
    private static final String PERSISTENCIA = BASE + ".persistencia..";
    private static final String MODELO = BASE + ".modelo..";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);

    private static void verificar(ArchRule regra) {
        // Regras sobre papéis que o projeto pode ainda não ter (ex.: fila) não falham por falta de classe
        var ajustada = regra.allowEmptyShould(true);
        (MIGRACAO ? FreezingArchRule.freeze(ajustada) : ajustada).check(CLASSES);
    }

    @Test
    void camadas() {
        verificar(layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Apresentacao").definedBy(APRESENTACAO)
                .layer("Negocio").definedBy(NEGOCIO)
                .layer("Integracao").definedBy(INTEGRACAO)
                .layer("Persistencia").definedBy(PERSISTENCIA)
                .layer("Modelo").definedBy(MODELO)
                .whereLayer("Apresentacao").mayNotBeAccessedByAnyLayer()
                .whereLayer("Negocio").mayOnlyBeAccessedByLayers("Apresentacao")
                .whereLayer("Integracao").mayOnlyBeAccessedByLayers("Negocio")
                .whereLayer("Persistencia").mayOnlyBeAccessedByLayers("Negocio", "Apresentacao")
                .whereLayer("Modelo").mayOnlyBeAccessedByLayers("Apresentacao", "Negocio", "Integracao", "Persistencia"));
    }

    @Test
    void todaClasseEstaNumaCamada() {
        verificar(classes().should().resideInAnyPackage(APRESENTACAO, NEGOCIO, INTEGRACAO, PERSISTENCIA, MODELO));
    }

    @Test
    void apresentacaoSoLeConsultasDaPersistencia() {
        // Gravar passa sempre pelo negócio (regras, transação); ler pode ir direto às projeções
        verificar(noClasses().that().resideInAPackage(APRESENTACAO)
                .should().dependOnClassesThat().resideInAPackage(BASE + ".persistencia.repositorio.."));
    }

    @Test
    void modeloSemDependenciaDeFramework() {
        // O modelo conhece só JPA (as anotações do mapeamento): nada de REST, CDI, fila ou Quarkus
        verificar(noClasses().that().resideInAPackage(MODELO)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "jakarta.enterprise..", "jakarta.inject..", "io.quarkus..", "io.smallrye..",
                        "org.eclipse.microprofile.."));
    }

    @Test
    void httpSoNaApresentacaoENaIntegracao() {
        verificar(noClasses().that().resideOutsideOfPackages(APRESENTACAO, INTEGRACAO)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.ws.rs..", "org.jboss.resteasy..", "io.quarkus.rest..", "org.eclipse.microprofile.rest.."));
    }

    @Test
    void pacotesDoNegocioNaoSeEnxergam() {
        // Cada pasta do negócio só usa a pasta do núcleo (negocio.processo no modelo de referência; troque se o nome
        // for outro), nunca outra pasta; e o núcleo não conhece nenhuma delas
        verificar(slices().matching(BASE + ".negocio.(*)..").should().notDependOnEachOther()
                .ignoreDependency(alwaysTrue(), resideInAPackage(BASE + ".negocio.processo..")));
    }

    @Test
    void jpaSoNoModeloENaPersistencia() {
        // A entidade é o modelo, mas o mapeamento e o acesso ao banco não passam disso: negócio, integração e
        // apresentação usam as entidades pelos getters e métodos de evento, sem API do JPA ou do Hibernate
        verificar(noClasses().that().resideOutsideOfPackages(MODELO, PERSISTENCIA)
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate.."));
    }

    @Test
    void entidadesNoModeloOuNaPersistencia() {
        // Entidade de negócio no modelo; tabela só técnica (ex.: chave de idempotência) na persistência
        verificar(classes().that().areAnnotatedWith(Entity.class).should().resideInAnyPackage(MODELO, PERSISTENCIA));
    }

    @Test
    void repositoriesNaPersistencia() {
        verificar(classes().that().areAnnotatedWith(Repository.class).should().resideInAPackage(PERSISTENCIA));
    }

    @Test
    void apiNaoDevolveEntidade() {
        // O contrato da API não muda quando o schema muda: resource devolve response ou projeção, nem List<Entidade>
        verificar(methods().that().areDeclaredInClassesThat().areAnnotatedWith(Path.class)
                .and().areDeclaredInClassesThat().areNotInterfaces().and().arePublic()
                .should(naoDevolverEntidade()));
    }

    @Test
    void transacaoSoNoNegocio() {
        // Quem decide o tamanho da transação é o negócio: QuarkusTransaction no fluxo, @Transactional nos CRUDs
        verificar(noClasses().that().resideOutsideOfPackage(NEGOCIO)
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.transaction..", "io.quarkus.narayana.."));
    }

    @Test
    void resourcesEClientesPeloNome() {
        // @Path em classe é endpoint de entrada; em interface com @RegisterRestClient, cliente de sistema externo
        verificar(classes().that().areAnnotatedWith(Path.class).and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Resource").andShould().resideInAPackage(BASE + ".apresentacao.rest.."));
        verificar(classes().that().areAnnotatedWith(RegisterRestClient.class)
                .should().haveSimpleNameEndingWith("Client").andShould().resideInAPackage(INTEGRACAO));
    }

    @Test
    void integracoesEServicosPeloNome() {
        // O que o negócio chama em cada sistema externo; as simulações do dev (@Alternative) ficam de fora
        verificar(classes().that().resideInAPackage(INTEGRACAO).and().areAnnotatedWith(ApplicationScoped.class)
                .and().areNotAnnotatedWith(Alternative.class)
                .should().haveSimpleNameEndingWith("Integracao"));
        if (PONTO_DE_EXTENSAO == null) {
            verificar(classes().that().resideInAPackage(NEGOCIO).and().areAnnotatedWith(ApplicationScoped.class)
                    .should().haveSimpleNameEndingWith("Service"));
        } else {
            var nome = PONTO_DE_EXTENSAO.substring(PONTO_DE_EXTENSAO.lastIndexOf('.') + 1);
            verificar(classes().that().implement(PONTO_DE_EXTENSAO)
                    .should().haveSimpleNameStartingWith(nome).andShould().resideInAPackage(NEGOCIO));
            verificar(classes().that().resideInAPackage(NEGOCIO).and().areAnnotatedWith(ApplicationScoped.class)
                    .and().doNotImplement(PONTO_DE_EXTENSAO)
                    .should().haveSimpleNameEndingWith("Service"));
        }
    }

    @Test
    void sistemasExternosNaoSeEnxergam() {
        verificar(slices().matching(BASE + ".integracao.(*)..").should().notDependOnEachOther());
    }

    @Test
    void canaisDeEntradaNaoSeEnxergam() {
        // A fila não reaproveita o corpo do REST, e vice-versa
        verificar(slices().matching(BASE + ".apresentacao.(*)..").should().notDependOnEachOther());
    }

    @Test
    void semCiclos() {
        verificar(slices().matching(BASE + ".(**)").should().beFreeOfCycles());
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
