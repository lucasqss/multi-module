# AGENTS.md — como trabalhar neste repositório

Instruções para quem gera ou altera código aqui, seja pessoa ou modelo de IA. São regras, não sugestões: a maioria é
verificada por teste e quebra o build. O **porquê** de cada decisão está em [`docs/adr/`](docs/adr/README.md). A
**explicação** para quem está chegando (visão, fluxos e conceitos, em linguagem simples) está em
[`docs/guia.html`](docs/guia.html).

## O projeto em 5 linhas

- Quarkus 3.33, Java 25, Oracle (Flyway), Maven multi-module e um único jar (`processo-app`).
- **Processos** de vários tipos (imobiliário, veicular) são criados, processados e cancelados por um **motor
  único**. O que muda por tipo fica num **segmento**.
- Os estados (`ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO`, `ABERTO → CANCELADO`) são o padrão State num enum.
- Um **cadastro simples** (agências) mostra o caminho curto para CRUD, sem motor.
- Entradas: REST e fila AMQP. Saídas: sistemas externos (cadastro, PDF) atrás de ports.

## Módulos e dependências permitidas

| Módulo | Pacote raiz | Contém | Pode depender de | Nunca depende de |
|---|---|---|---|---|
| `jpa-core` | `com.example.jpacore` | entidades JPA e migrations | Hibernate, Flyway | nenhum outro módulo |
| `processo-core` | `com.example.processo.core` | `motor`, `segmento.*`, `cadastro.*`: toda a regra | `jpa-core`, Jakarta Data | query, integrações, app, HTTP |
| `processo-query` | `com.example.processo.query` | consultas somente leitura de processos | `jpa-core`, Jakarta Data | `processo-core` |
| `integracoes` | `com.example.processo.integracoes` | um pacote por sistema externo | domínio e `port.out` do core, REST Client | app, query, outro sistema, serviços/SPI/adapters do core |
| `processo-app` | `com.example.processo.app` | entradas (`adapter/inbound/<canal>`), montagem, ITs | todos | (ninguém depende dele) |

Dentro do motor e de cada segmento, as pastas são `domain/` (Java puro), `application/` (`port/out`, `port/spi`,
`service`), `adapter/` (infraestrutura) e `config/`. A dependência vai sempre `adapter → application → domain`.

## Regras invioláveis (e o que as garante)

| Regra | Garantida por |
|---|---|
| Dependências da tabela acima; sem ciclos; toda classe pertence a um módulo registrado | `ArquiteturaModulosTest` |
| O motor não conhece segmentos; segmentos não se enxergam; `domain` é Java puro; `application` não conhece adapters | `ArquiteturaTest` (core) |
| Entradas chamam só serviços do core (`MotorDeProcessamento` ou o serviço de um cadastro), nunca ports, SPI, adapters ou entidades | `ArquiteturaModulosTest.appEntraNoCorePorServicos` |
| Um canal de entrada não usa classes de outro; o `dto` não conhece quem o recebe | `ArquiteturaModulosTest.entradasOrganizadasPorCanalEPapel` |
| HTTP só no app e nas integrações | `ArquiteturaModulosTest.httpSoNasBordas` |
| Fakes dos ports de persistência se comportam como o banco | `PersistenciaDoProcessoContrato` (fake no core, Oracle no app) |
| Enums do domínio e do jpa-core iguais (`Situacao`/`SituacaoProcesso`, `TipoImovel`) | `EnumsEspelhadosTest` |
| A máquina de estados aceita só as transições da tabela | `SituacaoTest` |
| O mesmo ciclo de vida para todo tipo, inclusive concorrência | `CicloDeVidaPorTipoIT` |
| Interface só onde há costura real (entre módulos, sistema externo, ponto de extensão, fake em teste) | revisão; [ADR 0005](docs/adr/0005-interface-so-onde-ha-costura-real.md) |

## Onde coloco…?

| Preciso de… | Vai em | Exemplo para copiar |
|---|---|---|
| Regra que vale para todo tipo de processo | `core/motor/domain/rule/RegrasComuns` | a regra de nome |
| Regra de um tipo | `core/segmento/<tipo>/domain/rule/Regras<Tipo>` | `RegrasVeiculares` |
| Decisão do processamento de um tipo | `avaliar()` no segmento | `SegmentoVeicular.avaliar` |
| Valor de domínio com validação (CPF, valor) | record em `core/motor/domain/model`, validando no construtor | `Cpf`, `Valor` |
| Estado ou transição nova | `core/motor/domain/state/Situacao`, mais o enum espelhado e o CHECK numa migration | `Situacao` |
| Erro de negócio novo | exceção em `domain/exception` + método no `TratadorDeErros` | `ConflitoDeSituacaoException` |
| Chamada a sistema externo | port em `application/port/out` + pacote em `integracoes/<sistema>` | `SituacaoCadastral` → `integracoes/cadastro` |
| Endpoint HTTP | `app/adapter/inbound/rest/resource` + request em `rest/dto` | `ProcessoVeicularResource` |
| Consumo de fila | `app/adapter/inbound/messaging/consumer` + mensagem em `messaging/dto` | `NovosProcessosImobiliarios` |
| Consulta/listagem de processos | `processo-query/repository/ProcessoQueries` + record em `query/dto` | `todos`, `ResumoProcesso` |
| Cadastro simples (CRUD) | `core/cadastro/<nome>` (pacote plano) + resource | `cadastro/agencia`, `AgenciasResource` |
| Tabela ou coluna | entidade em `jpa-core` + migration nova `V<n>__*.sql` | `V5__agencia.sql` |
| Configuração de sistema externo | `processo-app/src/main/resources/application.properties`, com URL por variável de ambiente | `quarkus.rest-client.cadastro.*` |

## Receitas (checklists)

Em cada receita, siga o exemplo indicado e rode `mvn verify` no fim.

### Novo tipo de processo (segmento)
Copie o veicular, que é o mais simples.
- [ ] `jpa-core`: entidade filha de `ProcessoEntity` com `@DiscriminatorValue` + migration da tabela filha (`V3__processo_veicular.sql`).
- [ ] `core/segmento/<tipo>/domain/model`: record que implementa `DadosDoSegmento` (`DadosVeiculares`).
- [ ] `core/segmento/<tipo>/domain/rule`: `Regras<Tipo>` com `Especificacao` (`RegrasVeiculares`).
- [ ] `core/segmento/<tipo>/application/port/out`: port que estende `PersistenciaDoSegmento<D>` (`ProcessosVeiculares`).
- [ ] `core/segmento/<tipo>/application/service`: `@Singleton` que estende `SegmentoBase<D>`, com só `regras()` e `avaliar()` (`SegmentoVeicular`).
- [ ] `core/segmento/<tipo>/adapter/outbound/persistence`: repository que estende o port + mapper (`ProcessoVeicularRepository`, `ProcessoVeicularMapper`). Valores monetários voltam com `setScale(2)`.
- [ ] `processo-query`: método de detalhe e record (`veicular(id)`, `DetalheVeicular`).
- [ ] `app/adapter/inbound/rest`: resource com `POST` e `GET {id}` + request com `paraSolicitacao()` (`ProcessoVeicularResource`, `NovoProcessoVeicularRequest`).
- [ ] Testes: `Segmento<Tipo>Test` no motor real (`SegmentoVeicularTest`) e um `Tipo` novo em `CicloDeVidaPorTipoIT.tipos()`.
- [ ] **Não** mexa no motor, na máquina de estados nem nos endpoints de ciclo de vida. Se precisar, o tipo não cabe no motor único (ADR 0002).

### Novo cadastro simples (CRUD)
Siga `core/cadastro/agencia` ([ADR 0012](docs/adr/0012-cadastro-simples-sem-motor.md)).
- [ ] `jpa-core`: entidade + migration.
- [ ] `core/cadastro/<nome>`: record válido por construção (lança `RegraVioladaException`), repository Jakarta Data, serviço `@ApplicationScoped` com `@Transactional` nas escritas e uma exceção de não encontrado.
- [ ] `app/adapter/inbound/rest`: resource (POST, GET, GET {id}, PUT, DELETE) + requests; a exceção de não encontrado mapeada no `TratadorDeErros`.
- [ ] IT do ciclo completo (`AgenciasIT`).
- [ ] **Não** use motor, ports, fakes nem `processo-query`.

### Nova integração com sistema externo
Siga `integracoes/pdf` ou `integracoes/cadastro` ([ADR 0008](docs/adr/0008-sistemas-externos-atras-de-ports.md)).
- [ ] Port em `application/port/out` do core (do motor ou do segmento que usa).
- [ ] `integracoes/<sistema>/client`: REST Client (`@RegisterRestClient(configKey = ...)`) + simulação `@IfBuildProfile("dev") @Alternative @Priority(1) @RestClient`.
- [ ] `integracoes/<sistema>/dto`: o formato do sistema externo, sem tipos do domínio.
- [ ] `integracoes/<sistema>/adapter`: implementa o port; `WebApplicationException | ProcessingException` → `SistemaExternoIndisponivelException`.
- [ ] `application.properties`: URL por variável de ambiente e timeouts de 2 s.
- [ ] Testes: adapter com cliente em lambda (`GeradorDeDocumentosAdapterTest`) e servidor falso no IT (`SistemaDePdfFalso`, `@WithTestResource`).

### Nova forma de iniciar um processo (canal de entrada)
Siga `app/adapter/inbound/messaging` ([ADR 0011](docs/adr/0011-entradas-por-canal-em-adapter-inbound.md)).
- [ ] `app/adapter/inbound/<canal>/dto` com o formato do canal e `paraSolicitacao()`.
- [ ] Quem recebe (`consumer/`, job) chama o motor. Fora do HTTP, `@ActivateRequestContext`.
- [ ] Fila: `motor.criar(solicitacao, idDaMensagem)`. Regra violada → responde recusado + ack; outra falha → nack. Aceite JSON como `JsonObject`, texto ou bytes.
- [ ] Teste com o canal em memória, reaproveitando o conjunto de `@WithTestResource` dos testes existentes (`NovosProcessosPorFilaTest`).

### Nova consulta
- [ ] Método `@Query` com `select new` em `ProcessoQueries` + record plano em `query/dto`. Paginação com `PageRequest`/`Page`.
- [ ] Endpoint no app. O HQL é validado na compilação.

### Novo módulo Maven
- [ ] `<module>` e `dependencyManagement` no POM pai; dependência direta no `processo-app` (o JaCoCo agregado só vê dependências diretas).
- [ ] `jandex-maven-plugin` no POM do módulo, se ele tiver beans.
- [ ] Pacote raiz em `MODULOS` no `ArquiteturaModulosTest` e as regras de dependência dele.

### Decisão de arquitetura nova
- [ ] Copie `docs/adr/0000-modelo.md` com o próximo número e acrescente no índice `docs/adr/README.md`. ADR aceita não se edita: escreva outra que a substitui.

## Convenções

- **Idioma:** classes, métodos, mensagens e docs em português; pastas estruturais em inglês (`domain`, `application`, `adapter`, `dto`…).
- **Records** para domínio, DTOs e projeções. Domínio imutável: cada evento devolve uma cópia (`Processo.cancelar()`).
- **Validação:** value objects validam no construtor; regras com `Especificacao` devolvem o motivo. Não há Bean Validation. Campos ausentes chegam nulos ao domínio para as regras recusarem com a mensagem certa.
- **Mensagens** de exceção de negócio são para o usuário final: viram o `detail` do Problem Details ou o `motivo` da resposta da fila.
- **Conversão:** de uma linha, como método do DTO (`paraSolicitacao()`, `de(p)`); em `mapper/` só quando há lógica (domínio ↔ entidade).
- **Transações:** no fluxo do processo, curtas e explícitas pelo port `Transacoes`, e regra ou chamada externa nunca dentro delas. Em cadastro simples, `@Transactional` no serviço.
- **Concorrência:** troca de situação por compare-and-set (`update ... where SITUACAO = :de`), sem lock pessimista; conflito → 409.
- **Erros HTTP:** `application/problem+json` com `type` fixo (`/problemas/...`). 422 regra, 404 não encontrado, 409 conflito, 400 parâmetro, 503 sistema externo.
- **Comentários:** Javadoc explica o porquê e o contrato, não repete o código.

## Não faça

- Interface com uma única implementação dentro do mesmo módulo (use a classe) — ADR 0005.
- Regra de negócio em resource, consumer ou DTO: eles só traduzem.
- Transação em volta do motor, ou chamada a sistema externo dentro de transação.
- Entidade JPA fora de `processo-core`/`processo-query`; tipos do domínio no `dto` de uma integração.
- `amqp-host`, URL do banco ou credenciais no `application.properties`: desliga o Dev Services e vaza ambiente. Use variáveis de ambiente.
- Editar migration já aplicada: crie outra.
- `@ApplicationScoped` em subclasse de `SegmentoBase`: precisa de `@Singleton` (proxy exigiria construtor vazio na base).
- Copiar o modelo do processo (motor, ports, fakes) para um CRUD — ADR 0012.
- Pular, desabilitar ou afrouxar teste para o build passar.

## Comandos

Pré-requisitos: Java 25, Maven 3.9+, Docker (Dev Services sobe Oracle e Artemis).

```bash
mvn -pl processo-core,integracoes -am test   # unitários, sem Docker (~30 s)
mvn verify                                    # tudo: contratos no Oracle, fila, ArchUnit e ITs contra o jar (~2-3 min)
mvn install -DskipTests && mvn -pl processo-app quarkus:dev   # dev; Swagger UI em http://localhost:8080/q/swagger-ui
```

## Definição de pronto

- [ ] `mvn verify` verde, sem teste pulado ou afrouxado.
- [ ] Comportamento novo tem teste: unitário no core (motor e segmento rodam sem Quarkus, com `Fakes`) e IT se passa por HTTP ou banco.
- [ ] Decisão nova ou mudada → ADR.
- [ ] Regra, receita ou armadilha mudou → `AGENTS.md`. Estrutura ou conceito mudou (módulo, fluxo, ideia nova) → também `docs/guia.html`. API ou comando mudou → `README.md`.

## Armadilhas conhecidas

- **`NUMBER` do Oracle perde zeros à direita** (`400000.00` → `400000`). `Valor` e os mappers normalizam para escala 2; nos testes, compare números, não texto.
- **`select new` não instancia records aninhados**: use projeções planas.
- **Enum inválido em `@QueryParam`** responde 404, não 400 (JAX-RS).
- **URL do banco em `%prod`** desliga o Dev Services nos ITs. O mesmo vale para `amqp-host`: em prod, o broker vem só por variável de ambiente (`AMQP_HOST`, `AMQP_PORT`, `AMQP_USERNAME`, `AMQP_PASSWORD`).
- **Colunas precisam de nome explícito**: o Quarkus não converte `dataAbertura` em `DATA_ABERTURA`.
- **Sessão stateless**: sem cascade nem cache de 1º nível; toda gravação é explícita.
- **Índice de entidades no jar**: sem o `copy-resources` do `entity/index` no `jpa-core`, o HQL dos outros módulos para de ser validado, sem aviso.
- **Consumidor sem request context**: fora do HTTP, ler pela `StatelessSession` fora de transação dá `ContextNotActiveException`. Use `@ActivateRequestContext`.
- **Fila virou tópico**: sem `capabilities=queue`, o Artemis cria o endereço como multicast, e as mensagens enviadas para a fila não chegam.
- **JSON chega como texto**: sem content-type JSON, o connector AMQP entrega `String`, não `JsonObject`. O consumidor aceita os dois.
- **Broker parado por disco**: o Artemis bloqueia produtores com o disco acima de 90%. No Dev Services isso está desligado; em produção, quem decide é a operação do broker.
- **`@QuarkusTest` com conjuntos diferentes de `@WithTestResource`** reinicia o app e o Oracle: reaproveite o conjunto existente.
- **Servidor falso nos ITs** roda na JVM do teste; o mapa devolvido por `start()` vira configuração do jar, que roda em outro processo.
- **ArchUnit** roda com a API core (`check`) dentro de `@Test` comum: o engine `archunit-junit5` não roda sob o JUnit 6 do BOM.
- **Subclasse de `SegmentoBase`** precisa ser `@Singleton`: `@ApplicationScoped` exige proxy, que exige construtor sem argumentos também na base.
- **`Unsatisfied dependency` de um bean que existe**: POM pai desatualizado no `~/.m2`. Rode `mvn install -N` na raiz.

## Por baixo dos panos

- **Repositories Jakarta Data** não têm implementação escrita à mão. Na compilação, o `hibernate-processor` gera a classe (ex.: `ProcessoQueries_`, em `target/generated-sources`) e valida o HQL contra as entidades, então campo errado é erro de compilação. O `jandex-maven-plugin` indexa o jar para o Quarkus achar o bean. No core, o repository estende o port, e os métodos `default` convertem pelo mapper.
- **Modelo de dados**: `PROCESSO` (pai, discriminador `SEGMENTO`) com uma tabela filha por tipo (herança JOINED, PK = FK). Numeração pela sequence única `SEQ_NUMERO_PROCESSO` (`<prefixo>-<ano>-<6 dígitos>`). Chaves de idempotência em `PROCESSO_IDEMPOTENCIA`. Cadastros têm tabela própria (`AGENCIA`). Tudo nas migrations de `jpa-core/src/main/resources/db/migration`.
- **Sonar e cobertura**: o CI roda `mvn clean verify` e depois o `sonar-maven-plugin` com `-DskipTests`. Todos os módulos leem o relatório JaCoCo agregado do `processo-app` (`sonar.coverage.jacoco.xmlReportPaths` no POM pai), e o `report-aggregate` só enxerga dependências diretas: módulo novo entra como dependência do app. `sonar.java.source` no POM pai é obrigatório (sem ele, o scanner 3.6 não entende records). Os ITs contam como testes, mas não como cobertura, porque rodam o jar em outro processo.
