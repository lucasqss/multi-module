# Design — Biblioteca CQRS (Quarkus 3.33 · Java 25 · Oracle)

## Objetivo

Exemplo público de Maven multi-module que demonstra:

- **CQRS com independência de design** (não de deploy): lado de escrita com domínio rico, lado de leitura com
  projeções; um lado não enxerga o código do outro.
- **Entidade de domínio separada da entidade JPA**, com o mapeamento do schema num único lugar.
- Um módulo de **geração de strings** com regra real: referência (NBR 6023) e citação (NBR 10520) ABNT.

Domínio: **Biblioteca** — livros, exemplares, leitores e empréstimos.

## Decisões

| Tema | Decisão |
|---|---|
| Módulos | `jpa-core`, `string-generator`, `biblioteca-command`, `biblioteca-query`, `biblioteca-app` |
| Domínio | dentro de `biblioteca-command` (pacote `domain`), Java puro; o query não tem domínio, só read models |
| `jpa-core` | só `@Entity` (JPA puro, campos privados, sem Panache) + migrations Flyway — a "verdade" do schema |
| Adapters | no `biblioteca-command`, implementando os ports do domínio sobre as entidades do `jpa-core` |
| Banco | o mesmo Oracle para os dois lados; Dev Services (`gvenzl/oracle-free`) em dev/teste; `docker-compose.yml` para o jar |
| Schema | Flyway + `quarkus.hibernate-orm.schema-management.strategy=validate` |
| Repositórios | **Jakarta Data** (`@Repository`, `@Find`, `@Query`, `@Insert`, `@Save`) sobre `StatelessSession`, com HQL validado em compilação pelo `hibernate-processor` |
| Deploy | único, via `biblioteca-app` **sem nenhuma classe Java** (pom + `application.properties` + testes): um uber-jar, `target/biblioteca.jar` |
| Build | um único `target/`, na raiz: cada módulo compila em `target/<artifactId>/`; relatórios de teste, cobertura e o jar ficam direto em `target/` |
| Testes | unitários puros (string-generator; domínio e casos de uso com fakes dos ports); ArchUnit guardando o pacote `domain` (command) e as fronteiras entre módulos (app); testes de API (`@QuarkusTest`, `*IT`) no `biblioteca-app` |
| Cobertura | agente do JaCoCo nos testes unitários e extensão `quarkus-jacoco` nos `*IT`, num único `target/jacoco/jacoco.exec`; relatório agregado gerado pela extensão em `target/site/jacoco-aggregate/` (só plugins Maven padrão e extensões Quarkus, sem antrun) |
| Versão anterior | o exemplo simples (`crud` + `string-generator`) está na branch `main` |

### Por que Jakarta Data e não Panache

Com domínio e entidade JPA separados, o fluxo do command é sempre "carrega → converte → aplica a regra →
converte de volta → **salva explicitamente**": o dirty checking de uma sessão stateful não tem função, e a
`StatelessSession` descreve honestamente o que o código faz. Os repositórios declaram só os métodos que
existem (o lado de leitura não herda `persist`/`delete`) e o HQL é verificado em compilação — renomear um
campo no `jpa-core` quebra o build dos módulos que o consultam. O Panache Next (`quarkus-data-hibernate`)
ainda é experimental na 3.33 e ficou de fora.

## Estrutura e dependências

```
multi-module/
├─ pom.xml               agregador: BOM Quarkus, versões internas, hibernate-processor, jandex, failsafe,
│                        target/ único, relatórios, JaCoCo, enforcer (JDK 25)
├─ .mvn/                 marca a raiz do projeto (o target/ único depende dele)
├─ docker-compose.yml    Oracle para rodar o jar empacotado
├─ jpa-core/             @Entity + db/migration            → quarkus-hibernate-orm, quarkus-flyway(-oracle)
├─ string-generator/     Java puro                         → nada
├─ biblioteca-command/   domain · application · adapter    → jpa-core, jakarta.data-api, quarkus-rest-jackson
├─ biblioteca-query/     @Repository de leitura · REST     → jpa-core, string-generator, jakarta.data-api, quarkus-rest-jackson
└─ biblioteca-app/       jar único + ITs + ArchUnit        → biblioteca-command, biblioteca-query, quarkus-jdbc-oracle
                         entre módulos + cobertura
```

`biblioteca-query` **não** depende de `biblioteca-command`: o Maven impede o lado de leitura de importar o
domínio. Os módulos com beans/entidades geram índice Jandex para o Quarkus descobri-los. O `string-generator`
não precisa de CDI: o query chama seus métodos estáticos.

## Command (`com.example.biblioteca.command`)

| Agregado | Dados | Operações |
|---|---|---|
| `Livro` | id, ISBN (13 dígitos), título, subtítulo, autores (ordem da obra), edição, local, editora, ano | cadastrar |
| `Exemplar` | id, livroId, tombo, situação `DISPONIVEL`/`EMPRESTADO`/`BAIXADO` | registrar, emprestar, devolver, baixar |
| `Leitor` | id, nome, e-mail, `suspensoAte` (último dia, inclusive) | cadastrar, suspender por atraso |
| `Emprestimo` | id, leitorId, exemplarId, data, dataPrevista (+14 dias), renovações, dataDevolucao | realizar, renovar, devolver |

Agregados são records imutáveis, referenciam-se **por id** e geram o próprio UUID. Datas vêm de um
`java.time.Clock` injetado (produzido em `adapter.Relogio`), o que torna os testes determinísticos.

**Specification:** `Especificacao<T>` devolve `Optional<String> violacao(T)` — vazio quando atendida, o motivo
quando não. Assim uma regra com vários motivos diz qual falhou; `e(...)` compõe regras e `exigir(T)` lança
`RegraVioladaException`.

- **Emprestar** (`LeitorPodeEmprestar`, regra que atravessa agregados via port `Emprestimos.abertosDo`): leitor
  não suspenso → nenhum empréstimo atrasado → menos de 3 abertos (a ordem define o motivo exibido); exemplar
  `DISPONIVEL`.
- **Renovar:** aberto, sem atraso e com menos de 2 renovações; o novo prazo conta do dia da renovação.
- **Devolver com atraso:** suspende o leitor 1 dia por dia de atraso, sem encurtar suspensão vigente.
- **Baixar exemplar:** proibido enquanto emprestado.

Camadas:
- `domain` — agregados, specifications, exceções e **ports** (`Livros`, `Exemplares`, `Leitores`, `Emprestimos`).
- `application` — `AcervoService`, `LeitorService`, `CirculacaoService`: `@Transactional`, orquestram
  carregar → regra → salvar.
- `adapter.persistence` — interfaces `@Repository` que **estendem o port**: métodos anotados são gerados pelo
  Hibernate Processor e `default` methods fazem o mapeamento domínio ↔ entidade.
- `adapter.rest` — recursos REST e `ErrosMapper` (`RegraVioladaException` → 422, `NaoEncontradoException` → 404).

REST: `POST /livros`, `POST /livros/{id}/exemplares`, `DELETE /exemplares/{id}`, `POST /leitores`,
`POST /emprestimos`, `POST /emprestimos/{id}/renovacao`, `POST /emprestimos/{id}/devolucao`.

## Query (`com.example.biblioteca.query`)

Repositórios somente-leitura `AcervoQueries` e `CirculacaoQueries` com `@Query` HQL. Autores são lidos a
partir de `AutoriaEntity` (`join fetch a.livro`) e agrupados por id do livro em Java (`Obras`); a contagem de
disponíveis é uma projeção `select new Disponiveis(...)` com `group by`, restrita aos livros do resultado.
`Obras` converte o read model para `ObraRef`/`AutorRef`, a entrada do `string-generator`.

- `GET /acervo?titulo=&autor=` → livros com **referência ABNT** e nº de exemplares disponíveis.
- `GET /leitores/{id}/painel` → empréstimos abertos com **citação** `(ASSIS, 1899)`, prazo e `atrasado`.
- `GET /emprestimos/atrasados` → relatório de atrasos com dias de atraso.

## string-generator (`com.example.stringgenerator`)

Entrada: `ObraRef(List<AutorRef> autores, titulo, subtitulo, edicao, local, editora, ano)`.

- `ReferenciaAbnt.de(obra)` → `ASSIS, Machado de. Dom Casmurro: romance. 2. ed. Rio de Janeiro: Garnier, 1899.`
- `CitacaoAbnt.de(obra)` → `(ASSIS, 1899)`, `(ASSIS; ALENCAR, 1899)`, `(ASSIS et al., 1899)`.
- `NomeAbnt.inverter` → `Machado de Assis` vira `ASSIS, Machado de`; sufixos de parentesco acompanham o
  sobrenome (`SILVA FILHO, João`), salvo quando só resta prenome + sufixo (`NETO, Paulo`).
- 1 a 3 autores separados por `; `; 4 ou mais → primeiro + `et al.`; 1ª edição omitida; sem local `[S. l.]`;
  sem editora `[s. n.]`.

## Persistência (`com.example.jpacore`)

`LivroEntity`, `AutoriaEntity`, `ExemplarEntity` (+ enum `SituacaoExemplar`), `LeitorEntity`, `EmprestimoEntity`.

- **Nenhuma coleção é mapeada** (ver armadilha 2). Associações são `@ManyToOne` unidirecionais.
- Chave estrangeira = coluna de id **gravável** (`livroId`) + associação **somente leitura**
  (`insertable = false, updatable = false`): o command grava ids, que é o que o domínio conhece; o query navega
  pela associação nos joins.
- IDs `UUID` ↔ `RAW(16)`; `isbn`, `tombo`, `email` únicos; índice único parcial garante no máximo um
  empréstimo aberto por exemplar, mesmo sob concorrência.
- Schema em `src/main/resources/db/migration/V1__schema.sql`.

## Testes

| Onde | O quê | Precisa de Docker |
|---|---|---|
| `string-generator` | casos ABNT (JUnit puro) | não |
| `biblioteca-command` | agregados, specifications, casos de uso com fakes em memória; ArchUnit das camadas | não |
| `biblioteca-app` | `ArquiteturaModulosTest`: fronteiras entre módulos (ArchUnit) | não |
| `biblioteca-app` | `*IT`: `@QuarkusTest` com a aplicação inteira (todos os módulos) e Oracle do Dev Services, na fase `verify` | sim |

Os ITs passam pela API HTTP: a massa entra pelo command e é verificada pelo query. Única exceção: o cenário de
atraso recua `DATA_PREVISTA` por JDBC (`DataSource` injetado), porque a API não controla o relógio da aplicação.

Os ITs são `@QuarkusTest`, e não `@QuarkusIntegrationTest`, por causa da cobertura (armadilha 10): eles sobem a
mesma aplicação, com todos os módulos, mas a partir das classes compiladas, e não do `target/biblioteca.jar`. O
empacotamento em si (configuração do uber-jar) não é exercitado pelos testes.

## Armadilhas encontradas

1. **O índice de entidades precisa ir no jar.** O Hibernate Processor grava `entity/index/<Entidade>` em
   `target/jpa-core/generated-sources/annotations` e, ao compilar outro módulo, procura esse índice no classpath. Sem ele,
   o HQL que cita entidades do `jpa-core` **não é validado — sem aviso**. Solução: `maven-resources-plugin`
   (`copy-resources` na fase `process-classes`) no `jpa-core/pom.xml`.
2. **Join por coleção desliga a validação — sem aviso.** Qualquer HQL que percorra uma coleção
   (`@ElementCollection` ou `@OneToMany`, com ou sem `fetch`) deixa de ser verificado, mesmo com a entidade no
   próprio módulo; joins pelo lado `@ManyToOne` são validados. Daí a regra de não mapear coleções.
3. **Sessão stateless:** sem cascade (autores gravados com `@Insert` explícito) e sem cache de 1º nível
   (agrupar resultados pelo id, não pela instância da entidade).
4. **`select new` não verifica tipos dos argumentos** em compilação (só nomes de atributos), e um record
   **aninhado** falha em runtime porque o Hibernate instancia pelo nome canônico. Projeções ficam em records
   top-level; os ITs cobrem o resto.
5. **Colisão de rotas no deploy único.** O JAX-RS escolhe primeiro a classe raiz mais específica e não volta
   atrás: com `@Path("/leitores")` no command, um `@Path("/")` + `leitores/{id}/painel` no query dá 404. Os
   recursos do query usam `@Path` de classe completo.
6. **Nomes de coluna explícitos.** O Quarkus não converte `dataPrevista` em `DATA_PREVISTA` por padrão; o
   `jpa-core` declara `@Column(name = ...)` nos campos compostos para não depender de configuração do app.
7. **ArchUnit com JUnit 6.** O engine `archunit-junit5` não executa sob o JUnit 6 do BOM (0 testes); usa-se a API
   core (`ClassFileImporter` + `check`) dentro de um `@Test` comum.
8. **Conexão do jar por variável de ambiente.** Uma URL de banco no `application.properties` sem perfil desliga
   o Dev Services nos testes (e, num `@QuarkusIntegrationTest`, até `%prod` desligaria, porque ele roda no
   perfil prod); o jar recebe a conexão por `QUARKUS_DATASOURCE_*`.
9. **BOM 3.33** gerencia `org.hibernate.orm:hibernate-processor` (7.2.x) e `jakarta.data:jakarta.data-api`
   (1.0.1); o `quarkus-data-processor` citado nos guias mais novos não existe nesta versão.
10. **Cobertura e classes transformadas.** O Quarkus reescreve algumas classes (entidades enriquecidas pelo
    Hibernate, repositórios Jakarta Data, recursos REST). Medida com o agente do JaCoCo, a cobertura dessas
    classes não casa com os `.class` de `target/*/classes` e é descartada (`Execution data for class ... does
    not match`); com `@QuarkusIntegrationTest` não há como evitar sem ler as classes de dentro do jar (o que
    exigiria antrun ou exec). A extensão `quarkus-jacoco` instrumenta **antes** das transformações, por isso os
    ITs são `@QuarkusTest`. Ela só instrumenta módulos com índice Jandex: o `string-generator` entra no
    relatório só com a cobertura dos próprios testes unitários.
11. **`target/` único e ferramentas que assumem `target/`.** O `ImportOption.DO_NOT_INCLUDE_TESTS` do ArchUnit
    procura `target/test-classes` e não reconhece `target/<artifactId>/test-classes` (filtro próprio nos testes);
    o `@QuarkusTest` assume `<módulo>/target/` quando as classes de teste estão fora do módulo (por isso todo
    caminho da `quarkus-jacoco` é absoluto, no failsafe do app); o `report-aggregate` do JaCoCo só procura `.exec`
    dentro da pasta de cada módulo (não é usado).
