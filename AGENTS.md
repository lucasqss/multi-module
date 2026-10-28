# AGENTS.md — como trabalhar neste repositório

Instruções para quem gera ou altera código aqui, seja pessoa ou modelo de IA. São regras, não sugestões: a maioria é
verificada por teste e quebra o build.

- O **porquê** de cada decisão está em [`docs/adr/`](docs/adr/README.md).
- Para **aplicar este modelo em outro projeto**, use o kit autocontido em
  [`docs/kit-camadas/`](docs/kit-camadas/README.md). Mudou uma decisão ou um padrão aqui? Atualize o kit
  (`python3 docs/kit-camadas/gerar-modelos.py` e o `GUIA.md`).
- A **explicação** para quem está chegando (visão, fluxos e conceitos, em linguagem simples) está em
  [`docs/guia.html`](docs/guia.html).

## O projeto em 5 linhas

- Quarkus 3.33, Java 25, Oracle (Flyway), **um módulo Maven** organizado em **camadas por pacote**
  ([ADR 0014](docs/adr/0014-camadas-num-modulo-so.md)).
- **Processos** de crédito de consórcio de várias **modalidades** (imobiliária, veicular) são criados, processados e
  cancelados por um **motor único** (`ProcessoService`). O que muda por modalidade fica numa implementação de
  `Modalidade`.
- Os estados (`ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO`, `ABERTO → CANCELADO`) são o padrão State num enum.
- Um **cadastro simples** (agências) mostra o caminho curto para CRUD, sem motor.
- **Entradas:** REST e fila AMQP. **Saídas:** sistemas externos (cadastro, PDF) na camada de integração.

## Camadas

Pacote raiz `com.example.processo`:

| Camada | Contém | Pode usar | Nunca usa |
|---|---|---|---|
| `apresentacao` | `rest/` e `mensageria/`, cada um com `dto/`; `TratadorDeErros` | `negocio`, `persistencia.consulta`, `modelo` | `persistencia.repositorio`, `integracao`, outro canal |
| `negocio` | `processo/` (o motor, `Modalidade`, regras comuns), uma pasta por modalidade, `agencia/` | `integracao`, `persistencia`, `modelo` | `apresentacao`; uma modalidade não usa a outra |
| `integracao` | uma pasta por sistema externo: cliente HTTP, simulação dev, formato externo, `*Integracao` | `modelo` | as outras camadas; um sistema não usa o outro |
| `persistencia` | `repositorio/` (gravar e ler entidades) e `consulta/` (projeções para as telas) | `modelo` | as outras camadas |
| `modelo` | entidades JPA, `Situacao`, `TipoImovel`, `Cpf`, `Valor`, exceções comuns | JPA | qualquer camada; REST, CDI, Quarkus |

**Gravar** passa sempre pelo negócio, por causa das regras e da transação. **Ler** vai da apresentação direto a
`persistencia.consulta`, sem um serviço que só repassaria.

## Regras invioláveis (e o que as garante)

| Regra | Garantida por |
|---|---|
| Dependências da tabela acima; toda classe numa camada; sem ciclos | `ArquiteturaTest` (`camadas`, `todaClasseEstaNumaCamada`, `semCiclos`) |
| Apresentação não grava direto: só lê de `persistencia.consulta` | `ArquiteturaTest.apresentacaoSoLeConsultasDaPersistencia` |
| Modalidades não se enxergam; o motor não conhece modalidades | `ArquiteturaTest.modalidadesNaoSeEnxergam` |
| Sistemas externos não se enxergam; canais de entrada não se enxergam | `ArquiteturaTest` |
| HTTP só na apresentação e na integração; o modelo não conhece framework além do JPA | `ArquiteturaTest` |
| JPA e Hibernate só em `modelo` e `persistencia`; `@Entity` só nelas; `@Repository` só na persistência | `ArquiteturaTest.jpaSoNoModeloENaPersistencia`, `entidadesNoModelo…` |
| A API não devolve entidade, nem dentro de `List`/`Optional` | `ArquiteturaTest.apiNaoDevolveEntidade` |
| Transação (`@Transactional`, `QuarkusTransaction`) só no negócio | `ArquiteturaTest.transacaoSoNoNegocio` |
| Nomes pelo papel: `*Resource`, `*Client`, `*Integracao`, `Modalidade*`, `*Service` | `ArquiteturaTest.nomesPeloPapel` |
| A máquina de estados aceita só as transições da tabela | `SituacaoTest` |
| O mesmo ciclo de vida para toda modalidade, inclusive concorrência | `CicloDeVidaPorTipoIT` |
| Gravação polimórfica, compare-and-set e chave de idempotência funcionam no Oracle | `ProcessoRepositoryTest` |
| O PDF leva só os atributos da modalidade, mesmo com o enhancement do Hibernate | `ProcessoServiceTest` |
| Interface só com mais de uma implementação real (`Modalidade`, clientes HTTP) | revisão; [ADR 0014](docs/adr/0014-camadas-num-modulo-so.md) |

## Onde coloco…?

| Preciso de… | Vai em | Exemplo para copiar |
|---|---|---|
| Regra que vale para toda modalidade | `negocio/processo/RegrasComuns` | a regra de nome |
| Regra de uma modalidade | `negocio/<modalidade>/Regras<Modalidade>` | `RegrasVeiculares` |
| Regra que consulta o banco | na `Modalidade<…>`, usando um repository | `ModalidadeImobiliaria.matriculaSemProcessoEmAndamento` |
| Decisão do processamento de uma modalidade | `avaliar()` na `Modalidade<…>` | `ModalidadeVeicular.avaliar` |
| Campo novo num processo | migration + atributo na entidade + request (+ projeção, se a tela mostra) | `ProcessoVeicular.valorFipe` |
| Valor com validação (CPF, valor) | record em `modelo`, validando no construtor | `Cpf`, `Valor` |
| Estado ou transição nova | `modelo/Situacao` + o CHECK numa migration nova | `Situacao` |
| Erro de negócio novo | exceção em `negocio/<pacote>` (ou em `modelo`, se a integração também lança) + método no `TratadorDeErros` | `ConflitoDeSituacaoException` |
| Chamada a sistema externo | `integracao/<sistema>`: cliente + `<Sistema>Integracao` | `integracao/cadastro` |
| Endpoint HTTP | `apresentacao/rest` + request/response em `rest/dto` | `ProcessoVeicularResource` |
| Consumo de fila | `apresentacao/mensageria` + mensagem em `mensageria/dto` | `NovosProcessosImobiliarios` |
| Consulta/listagem de processos | `persistencia/consulta/ProcessoConsultas` + record de projeção | `todos`, `ResumoProcesso` |
| Leitura de um cadastro simples | o próprio serviço do cadastro | `AgenciaService.porCodigo` |
| Cadastro simples (CRUD) | entidade em `modelo` + repository + `negocio/<nome>/<Nome>Service` + resource | `Agencia`, `AgenciaService`, `AgenciasResource` |
| Tabela ou coluna | entidade em `modelo` + migration nova `V<n>__*.sql` | `V5__agencia.sql` |
| Configuração de sistema externo | `application.properties`, com URL por variável de ambiente | `quarkus.rest-client.cadastro.*` |

## Receitas (checklists)

Em cada receita, siga o exemplo indicado e rode `mvn verify` no fim.

### Nova modalidade de processo

Copie a veicular, que é a mais simples.

- [ ] `modelo`: entidade filha de `Processo` com `@DiscriminatorValue(SEGMENTO)` e construtor de pedido
  (`ProcessoVeicular`). Também a migration da tabela filha (`V3__processo_veicular.sql`).
- [ ] `negocio/<modalidade>`:
  - `Regras<Modalidade>` com `Especificacao` (`RegrasVeiculares`);
  - `Modalidade<Entidade>` `@ApplicationScoped` com `tipo()`, `prefixo()`, `regras()` e `avaliar()`
    (`ModalidadeVeicular`).
- [ ] `persistencia/consulta`: método de detalhe e projeção (`veicular(id)`, `DetalheVeicular`). Repository próprio só
  se houver consulta da modalidade (`ProcessoImobiliarioRepository`).
- [ ] `apresentacao/rest`: resource com `POST` e `GET {id}`, mais um request com `paraModelo()`
  (`ProcessoVeicularResource`, `NovoProcessoVeicularRequest`).
- [ ] Testes:
  - `Modalidade<…>Test` puro (`ModalidadeVeicularTest`);
  - um `Tipo` novo em `CicloDeVidaPorTipoIT.tipos()`.
- [ ] **Não** mexa no `ProcessoService`, na `Situacao` nem nos endpoints de ciclo de vida. Se precisar, a modalidade
  não cabe no motor único (ADR 0002).

### Novo cadastro simples (CRUD)

Siga `agencia` ([ADR 0012](docs/adr/0012-cadastro-simples-sem-motor.md)).

- [ ] `modelo`: entidade válida por construção (lança `RegraVioladaException`), mais a migration.
- [ ] `persistencia/repositorio`: repository Jakarta Data.
- [ ] `negocio/<nome>`: serviço `@ApplicationScoped` com `@Transactional` nas escritas e uma exceção de não encontrado.
- [ ] `apresentacao/rest`:
  - resource (POST, GET, GET {id}, PUT, DELETE), requests e um response (a entidade não sai na API);
  - a exceção de não encontrado mapeada no `TratadorDeErros`.
- [ ] IT do ciclo completo (`AgenciasIT`).
- [ ] **Não** use motor, `Modalidade` nem `ProcessoConsultas`.

### Nova integração com sistema externo

Siga `integracao/pdf` ou `integracao/cadastro`.

- [ ] `integracao/<sistema>`:
  - REST Client (`@RegisterRestClient(configKey = ...)`);
  - simulação `@IfBuildProfile("dev") @Alternative @Priority(1) @RestClient`;
  - o formato do sistema externo (records só com tipos simples);
  - `<Sistema>Integracao` `@ApplicationScoped`, que traduz a resposta e converte
    `WebApplicationException | ProcessingException` em `SistemaExternoIndisponivelException`.
- [ ] O negócio injeta a `<Sistema>Integracao` direto, sem interface.
- [ ] `application.properties`: URL por variável de ambiente e timeouts de 2 s.
- [ ] Testes:
  - a integração com cliente em lambda (`PdfIntegracaoTest`);
  - servidor falso nos testes com Quarkus (`SistemaDePdfFalso`, `@WithTestResource`).

### Nova forma de iniciar um processo (canal de entrada)

Siga `apresentacao/mensageria` ([ADR 0011](docs/adr/0011-entradas-por-canal-em-adapter-inbound.md), hoje dentro da
[0014](docs/adr/0014-camadas-num-modulo-so.md)).

- [ ] `apresentacao/<canal>/dto` com o formato do canal e `paraModelo()`.
- [ ] Quem recebe (consumidor, job) chama o `ProcessoService`. Fora do HTTP, use `@ActivateRequestContext`.
- [ ] Fila:
  - criar com `motor.criar(processo, idDaMensagem)`;
  - regra violada → responde recusado + ack; outra falha → nack;
  - aceite JSON como `JsonObject`, texto ou bytes.
- [ ] Teste com o canal em memória, reaproveitando o conjunto de `@WithTestResource` dos testes existentes
  (`NovosProcessosPorFilaTest`).

### Nova consulta

- [ ] Método `@Query` com `select new` em `ProcessoConsultas` + record plano em `persistencia/consulta`. Paginação com
  `PageRequest`/`Page`.
- [ ] Endpoint na apresentação chamando a consulta direto. O HQL é validado na compilação.

### Decisão de arquitetura nova

- [ ] Copie `docs/adr/0000-modelo.md` com o próximo número e acrescente no índice `docs/adr/README.md`. ADR aceita não
  se edita: escreva outra que a substitui.

## Convenções

- **Idioma:**
  - classes, métodos, mensagens e docs em português;
  - sufixos de papel: `*Resource`, `*Service`, `*Integracao`, `*Repository`, `*Request`, `*Response`.
- **Entidades são o modelo:**
  - têm construtor de pedido (sem id) para a apresentação;
  - têm um construtor sem argumentos protegido, só para o Hibernate;
  - getters, e métodos de evento (`abrir`, `cancelar`…) no lugar de setters.
- **Records** para value objects, DTOs e projeções.
- **Validação:**
  - value objects validam no construtor;
  - regras com `Especificacao` devolvem o motivo;
  - não há Bean Validation;
  - campos ausentes chegam nulos à entidade para as regras recusarem com a mensagem certa.
- **Mensagens** de exceção de negócio são para o usuário final: viram o `detail` do Problem Details ou o `motivo` da
  resposta da fila.
- **Conversão** em método do DTO (`paraModelo()`, `de(p)`); não há mapper.
- **Transações:**
  - no motor, curtas e explícitas (`QuarkusTransaction.requiringNew()`), e regra ou chamada externa nunca dentro
    delas;
  - em cadastro simples, `@Transactional` no serviço.
- **Concorrência:** troca de situação por compare-and-set (`update ... where SITUACAO = :de`), sem lock pessimista;
  conflito → 409.
- **Erros HTTP:** `application/problem+json` com `type` fixo (`/problemas/...`).
  - 422 regra;
  - 404 não encontrado;
  - 409 conflito;
  - 400 parâmetro;
  - 503 sistema externo.
- **Comentários:** o Javadoc explica o porquê e o contrato, não repete o código.

## Não faça

- Interface com uma única implementação: use a classe.
- Regra de negócio em resource, consumidor ou DTO: eles só traduzem.
- Gravar pela apresentação (resource → repository): grave pelo negócio.
- API do JPA ou do Hibernate fora de `modelo` e `persistencia`: as outras camadas usam a entidade pelos getters e métodos de evento.
- Tabela só técnica (ex.: `ChaveIdempotencia`) no `modelo`: ela fica na persistência, escondida atrás do repository.
- Entidade na resposta da API: use um response.
- Transação em volta do motor, ou chamada a sistema externo dentro de transação.
- Tipos do modelo no formato de um sistema externo (`integracao/<sistema>`).
- `amqp-host`, URL do banco ou credenciais no `application.properties`: desliga o Dev Services e vaza ambiente. Use
  variáveis de ambiente.
- Editar migration já aplicada: crie outra.
- Copiar o motor ou a `Modalidade` para um CRUD (ADR 0012).
- Pular, desabilitar ou afrouxar teste para o build passar.

## Comandos

Pré-requisitos: Java 25, Maven 3.9+, Docker (o Dev Services sobe Oracle e Artemis).

```bash
mvn test -DexcludedGroups=banco   # testes puros (regras, estados, integrações, ArchUnit), sem Docker (~20 s)
mvn verify                        # tudo: testes com Oracle, fila, ArchUnit e ITs contra o jar (~3-4 min)
mvn quarkus:dev                   # dev; Swagger UI em http://localhost:8080/q/swagger-ui
```

Todo `@QuarkusTest` leva `@Tag("banco")`: é o que o deixa fora do comando rápido.

## Definição de pronto

- [ ] `mvn verify` verde, sem teste pulado ou afrouxado.
- [ ] Comportamento novo tem teste:
  - unitário puro para regra, estado e value object;
  - `@QuarkusTest` para o que passa pelo motor ou pelo banco;
  - IT se passa por HTTP.
- [ ] Decisão nova ou mudada → ADR.
- [ ] Regra, receita ou armadilha mudou → `AGENTS.md`. Estrutura ou conceito mudou (camada, fluxo, ideia nova) →
  também `docs/guia.html`. API ou comando mudou → `README.md`.

## Armadilhas conhecidas

- **`NUMBER` do Oracle perde zeros à direita** (`400000.00` vira `400000`). Nos testes, compare números
  (`compareTo`), não texto.
- **`select new` não instancia records aninhados**: use projeções planas.
- **Enum inválido em `@QueryParam`** responde 404, não 400 (JAX-RS).
- **URL do banco em `%prod`** desliga o Dev Services nos ITs. O mesmo vale para `amqp-host`: em prod, o broker vem só
  por variável de ambiente (`AMQP_HOST`, `AMQP_PORT`, `AMQP_USERNAME`, `AMQP_PASSWORD`).
- **Colunas precisam de nome explícito**: o Quarkus não converte `dataAbertura` em `DATA_ABERTURA`.
- **Sessão stateless**: sem cascade nem cache de 1º nível. Toda gravação é explícita, e mudar um atributo da entidade
  não grava nada sozinho: a troca de situação é um `update` do repository.
- **O discriminador é só leitura**: quem grava a coluna SEGMENTO é o `@DiscriminatorValue`. O construtor da filha
  preenche o atributo `segmento` para o processo recém-criado (ex.: o PDF) já o ter.
- **Consumidor sem request context**: fora do HTTP, ler pela `StatelessSession` fora de transação dá
  `ContextNotActiveException`. Use `@ActivateRequestContext`.
- **Fila virou tópico**: sem `capabilities=queue`, o Artemis cria o endereço como multicast, e as mensagens enviadas
  para a fila não chegam.
- **JSON chega como texto**: sem content-type JSON, o connector AMQP entrega `String`, não `JsonObject`. O consumidor
  aceita os dois.
- **Broker parado por disco**: o Artemis bloqueia produtores com o disco acima de 90%. No Dev Services isso está
  desligado; em produção, quem decide é a operação do broker.
- **`@QuarkusTest` com conjuntos diferentes de `@WithTestResource`** reinicia o app e o Oracle: reaproveite o conjunto
  existente (`FilaEmMemoria`, `CadastroFalso`, `SistemaDePdfFalso`).
- **Servidor falso nos ITs** roda na JVM do teste; o mapa devolvido por `start()` vira configuração do jar, que roda
  em outro processo.
- **Cobertura de classe transformada no jar**: o Quarkus muda no build os beans `@ApplicationScoped` sem construtor
  vazio e as entidades (enhancement). O que só os ITs exercitam nessas classes não entra na cobertura (o JaCoCo avisa
  "do not match"). Se o gate cobrar, cubra com `@QuarkusTest`: lá o `quarkus-jacoco` instrumenta antes da
  transformação.
- **ArchUnit** roda com a API core (`check`) dentro de `@Test` comum: o engine `archunit-junit5` não roda sob o JUnit 6
  do BOM.

## Por baixo dos panos

- **Repositories Jakarta Data** não têm implementação escrita à mão.
  - Na compilação, o `hibernate-processor` gera a classe (ex.: `ProcessoRepository_`, em `target/generated-sources`).
  - Ele também valida o HQL contra as entidades: campo errado é erro de compilação.
  - `ProcessoRepository.inserir(Processo)` e `buscar(id)` são polimórficos: gravam e devolvem a filha certa.
- **Modelo de dados:**
  - `PROCESSO` é a tabela pai, com o discriminador `SEGMENTO`, e há uma tabela filha por modalidade (herança JOINED,
    PK = FK).
  - A numeração vem da sequence única `SEQ_NUMERO_PROCESSO` (`<prefixo>-<ano>-<6 dígitos>`).
  - As chaves de idempotência ficam em `PROCESSO_IDEMPOTENCIA`, e os cadastros têm tabela própria (`AGENCIA`).
  - Tudo está nas migrations de `src/main/resources/db/migration`.
- **Sonar e cobertura:**
  - O CI roda `mvn clean verify` e depois o `sonar-maven-plugin` com `-DskipTests`.
  - A cobertura vem de `target/site/jacoco/jacoco.xml`. Ele une três fontes em `target/jacoco-merged.exec`:
    - os testes puros, pelo agente do JaCoCo: `jacoco.exec`;
    - os `@QuarkusTest`, pelo `quarkus-jacoco`: `jacoco-quarkus.exec`;
    - os ITs, pelo agente no jar via `quarkus.test.arg-line` no failsafe: `jacoco-it.exec`.
  - `sonar.java.source` no POM é obrigatório: sem ele, o scanner 3.6 não entende records.
