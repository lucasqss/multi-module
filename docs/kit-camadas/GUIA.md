# Guia para aplicar o modelo em camadas num projeto Quarkus existente

Este guia é para o **agente de IA** (Copilot CLI ou outro) que vai migrar um projeto Quarkus + Oracle para o modelo
em camadas. Ele é autocontido: tudo o que você precisa está nesta pasta.

- **Modelos de código** (arquivos reais do projeto de referência, que compilam e passam nos testes): `MODELOS.md`.
- **Regras de arquitetura** prontas: `ArquiteturaTest.java`.
- **Instruções permanentes** para o projeto migrado: `AGENTS.md`.

Leia este guia inteiro antes da Fase 0. Em caso de dúvida entre este guia e o código existente, **pare e pergunte**.
Não invente regra de negócio.

---

## 1. Objetivo e premissas

**Objetivo:** um único módulo Maven, dividido em **cinco pacotes de camada**, com as fronteiras garantidas por testes
do ArchUnit. Separa quatro tipos de lógica:

- a do sistema (negócio);
- a de sistemas externos (integração);
- a de banco (persistência);
- a de apresentação (REST, fila).

As quatro usam um vocabulário comum, o modelo.

**Premissas que justificam o modelo.** Confira se valem no projeto alvo; se alguma não valer, registre no diagnóstico.

- **Carga baixa ou moderada**: um monolito dá conta. No projeto de referência são até 1.000 usuários por dia em
  horário comercial, menos de 1 requisição por segundo.
- **Integridade transacional importa**, por exemplo dinheiro: o que muda junto fica numa transação local.
- **Domínio em evolução**: o modelo de dados muda com frequência, e cada camada a menos é um lugar a menos para mudar.
- **Um time**, ou poucos, com a mesma cadência de entrega.

**Quando o modelo NÃO se aplica.** Nestes casos, não force; registre e pergunte.

- uma parte precisa de disponibilidade ou escala diferente (24x7 enquanto o resto não; picos imprevisíveis);
- uma parte tem escopo de segurança ou compliance próprio (dados de cartão, PCI);
- vários times disputam o mesmo artefato com cadências diferentes.

Referências que fundamentam as escolhas:

- Mark Richards e Neal Ford, *Fundamentals of Software Architecture* (estilo em camadas, camadas abertas e fechadas,
  *architecture sinkhole*, *fitness functions*) e *Software Architecture: The Hard Parts* (granularidade);
- Kent Beck, *Extreme Programming Explained* (design simples, YAGNI) e *Tidy First?* (acoplamento, opções,
  reversibilidade).

---

## 2. Decisões técnicas

Formato de cada decisão: **o quê**, **por quê**, **como aplicar** e **o que garante**.

### 2.1 Um módulo, cinco camadas por pacote

- **O quê:** um `pom.xml` sem submódulos. Abaixo de `<pacote-base>`, os pacotes `apresentacao`, `negocio`,
  `integracao`, `persistencia` e `modelo`.
- **Por quê:** separar módulos Maven e usar hexagonal dentro deles gerava muitas pastas, ports com uma implementação
  só e mappers. A equipe achou confuso, e cada campo novo atravessava 6 arquivos. As camadas separam exatamente os
  quatro tipos de lógica que interessam.
- **Como aplicar:** a seção 3 traz a árvore e a tabela de dependências.
- **Garante:** `ArquiteturaTest.camadas` e `todaClasseEstaNumaCamada`.

### 2.2 A entidade JPA é o modelo, e o JPA fica confinado

- **O quê:**
  - As entidades JPA são o modelo de domínio: não há record de domínio separado nem mapper.
  - O JPA e o Hibernate só aparecem em `modelo` (as anotações de mapeamento) e em `persistencia` (os repositories).
  - Negócio, integração e apresentação usam a entidade só pelos getters e pelos métodos de evento.
- **Por quê:**
  - O mapper dá pureza, mas custa 3 lugares a mais por campo, e o benefício que ele traz (trocar o banco ou o ORM)
    raramente é usado.
  - Confinar o JPA mantém a dependência pequena e visível.
- **Como aplicar:**
  - a entidade tem um construtor "de pedido" (sem id, número nem data) para a apresentação criar;
  - tem um construtor sem argumentos `protected`, só para o Hibernate;
  - tem getters, sem setters;
  - mudanças de estado são métodos com nome de evento: `abrir`, `cancelar`, `recusar(motivo)`;
  - tabelas puramente técnicas (por exemplo, chave de idempotência) são entidades **dentro de `persistencia`**, com
    visibilidade de pacote, escondidas atrás do repository.
- **Garante:**
  - `jpaSoNoModeloENaPersistencia`;
  - `entidadesNoModeloOuNaPersistencia`;
  - `repositoriesNaPersistencia`;
  - `modeloSemDependenciaDeFramework`.

### 2.3 Gravar pelo negócio; ler direto pelas consultas

- **O quê:**
  - A apresentação só grava chamando um serviço do negócio.
  - Para ler, ela pode chamar direto as consultas de `persistencia.consulta`, que são projeções com `select new`.
- **Por quê:**
  - Gravar exige regras e transação.
  - Ler para uma tela não exige: um serviço que só repassa a chamada é o anti-padrão *architecture sinkhole*.
  - É o equivalente prático a separar escrita e leitura (CQRS) sem módulo próprio.
- **Garante:** `apresentacaoSoLeConsultasDaPersistencia` e `camadas`.

### 2.4 Interface só com mais de uma implementação real

- **O quê:** interfaces só para:
  - o ponto de extensão do negócio (no modelo de referência, `Modalidade`, uma implementação por tipo de processo);
  - os clientes HTTP, porque o REST Client exige interface.

  O resto é classe concreta: o serviço que o negócio chama na integração, os serviços, os repositories (Jakarta Data
  gera a implementação).
- **Por quê:** interface com uma implementação é indireção sem ganho.
- **Garante:** revisão de código e `integracoesEServicosPeloNome`.

### 2.5 Motor único com ponto de extensão (quando há variações de um mesmo fluxo)

- **O quê:** quando há vários *tipos* de uma mesma coisa com o mesmo ciclo de vida (no modelo de referência, as
  modalidades de processo), há:
  - **um serviço-motor** que faz o que é igual para todos: ordem dos passos, regras comuns, numeração, gravação,
    estados;
  - **uma interface** com uma implementação por tipo, que traz `tipo()`, `prefixo()`, `regras()` e `avaliar()`.
- **Por quê:** tipo novo entra sem mexer no motor.
- **Como aplicar:**
  - entidade pai abstrata com `@Inheritance(JOINED)` e uma filha por tipo com `@DiscriminatorValue`;
  - o motor recebe `@Any Instance<Modalidade<?>>` e escolhe pelo `tipo().isInstance(entidade)`.
- **Quando NÃO usar:** se só existe um tipo, não crie a interface. Se um tipo precisa de estados ou passos que os
  outros não têm, ele ganha um fluxo próprio.
- **Garante:** `pacotesDoNegocioNaoSeEnxergam` (um tipo não usa o outro; o motor não conhece nenhum) e um IT
  parametrizado por tipo.

### 2.6 Estados com o padrão State num enum

- **O quê:**
  - Um `enum` com uma constante por estado.
  - Cada constante sobrescreve só os eventos que aceita e devolve o próximo estado.
  - Por padrão, todo evento lança `TransicaoInvalidaException`, que vira 422.
  - A entidade guarda o estado e delega a ele (`situacao = situacao.cancelar()`).
- **Por quê:** a máquina inteira num arquivo, sem `if` espalhado. O nome da constante é o valor persistido.
- **Como aplicar:** estado novo → constante nova + o CHECK do banco numa migration nova.
- **Garante:** um teste em tabela que cobre todos os pares (estado, evento) (`SituacaoTest` em `MODELOS.md`).

### 2.7 Concorrência por compare-and-set

- **O quê:** a troca de estado é um `update ... set SITUACAO = :para where ID = :id and SITUACAO = :de`. Se nenhuma
  linha mudou, outra requisição chegou antes, e a resposta é `ConflitoDeSituacaoException` (409).
- **Por quê:** sem lock pessimista e sem segurar conexão.
- **Como aplicar:**
  - `@SQL` nativo no repository, porque um update HQL numa hierarquia JOINED gera tabelas temporárias no Oracle;
  - um método `default` que devolve `boolean`.
- **Garante:** teste de repository no Oracle e um IT com 8 requisições simultâneas.

### 2.8 Value objects válidos por construção

- **O quê:** `record`s como `Cpf` e `Valor` validam no construtor e lançam `RegraVioladaException`, que vira 422 com a
  mensagem.
- **Por quê:** quem recebe não valida de novo.
- **Como aplicar:**
  - a conversão acontece onde o texto entra no sistema, no `paraModelo()` do request;
  - a entidade guarda o tipo simples (`String`, `BigDecimal`);
  - o `Valor` normaliza a escala para 2, porque o `NUMBER` do Oracle perde os zeros à direita.

### 2.9 Regras com `Especificacao`

- **O quê:** uma interface funcional `violacao(T) → Optional<String>`, com `e(...)` para compor e `exigir(...)` para
  lançar.
- **Por quê:** a regra que falhou diz o motivo; composição em ordem (comuns → do tipo → externas).
- **Como aplicar:**
  - regra que depende só do pedido fica numa classe `Regras<Tipo>` com constantes;
  - regra que consulta o banco fica na implementação do ponto de extensão, por último;
  - a chamada a sistema externo vem depois de todas, para não chamar à toa.

### 2.10 Transações curtas e explícitas

- **O quê:**
  - No fluxo do motor, `QuarkusTransaction.requiringNew().run/call(...)` só em volta das gravações.
  - Regras e chamadas a sistema externo ficam fora da transação.
  - No CRUD simples, `@Transactional` no serviço.
- **Por quê:** não segurar conexão nem transação esperando outro sistema.
- **Garante:** `transacaoSoNoNegocio`.

### 2.11 Efeito externo depois do commit

- **O quê:** pedir o PDF (ou qualquer efeito em outro sistema) **depois** do commit. Se falhar, o processo continua
  criado e a falha vai para o log.
- **Por quê:** o que não foi gravado não é impresso, e uma falha externa não desfaz o negócio.
- **Ponto em aberto:** não há retentativa. Se o projeto alvo precisar, use outbox: uma tabela gravada na transação e
  um job que reenvia.

### 2.12 Idempotência por chave

- **O quê:** `criar(entidade, chave)`. Se a chave já existe, devolve o que foi criado da primeira vez, antes de
  qualquer regra. A chave é gravada na mesma transação da entidade, numa tabela com PK na chave.
- **Por quê:** fila reentrega mensagens; o id da mensagem é a chave.

### 2.13 Erros em Problem Details (RFC 9457)

- **O quê:** um `TratadorDeErros` com `@ServerExceptionMapper` por exceção. Resposta `application/problem+json` com
  `type` fixo (`/problemas/<tipo>`), `title`, `status` e `detail`, que traz a mensagem de negócio.
- **Mapa:**
  - 422: regra violada ou transição inválida;
  - 404: não encontrado (inclusive o `NotFoundException` do JAX-RS);
  - 409: conflito;
  - 400: parâmetro inválido;
  - 503: sistema externo fora do ar.

### 2.14 Entradas por canal

- **O quê:** `apresentacao/rest` e `apresentacao/mensageria`, cada um com seu `dto/`. Um canal não usa as classes do
  outro.
- **Como aplicar (fila AMQP):**
  - `@Incoming` + `@Blocking` + `@ActivateRequestContext`;
  - payload como `JsonObject`, `String`, `Buffer` ou `byte[]`;
  - regra violada ou mensagem malformada → resposta "recusado" + ack;
  - outra falha → nack (reentrega);
  - `capabilities=queue` na configuração.
- **Garante:** `canaisDeEntradaNaoSeEnxergam`.

### 2.15 Integração: cliente + simulação + `*Integracao`

- **O quê:** uma pasta por sistema externo com:
  - `*Client` (REST Client, `configKey`);
  - `*ClientSimulado` (`@IfBuildProfile("dev") @Alternative @Priority(1) @RestClient`);
  - records no formato do sistema externo;
  - `*Integracao` `@ApplicationScoped`, que o negócio chama. Ela traduz a resposta e converte
    `WebApplicationException | ProcessingException` em `SistemaExternoIndisponivelException`.
- **Por quê:** o formato e os erros do sistema externo não vazam, e o dev roda sem os sistemas reais.
- **Como aplicar:**
  - URL por variável de ambiente;
  - timeouts de 2 s;
  - nos testes, um servidor HTTP falso (`com.sun.net.httpserver`) via `QuarkusTestResourceLifecycleManager`.
- **Garante:** `sistemasExternosNaoSeEnxergam`, `resourcesEClientesPeloNome` e `integracoesEServicosPeloNome`.

### 2.16 CRUD pelo caminho curto

- **O quê:** o que é só cadastro (incluir, alterar, excluir, consultar), sem estados nem fluxo, leva:
  - uma entidade válida por construção em `modelo`;
  - um repository Jakarta Data;
  - um `*Service` com `@Transactional` nas escritas;
  - um resource com response próprio, porque a entidade não sai na API.
- **Por quê:** copiar o motor para um CRUD é cerimônia.

### 2.17 Persistência com Jakarta Data e HQL validado na compilação

- **O quê:** repositories `@Repository` (Jakarta Data), com `@Find`, `@Insert`, `@Update`, `@Delete`, `@Query` (HQL),
  `@SQL` (nativo) e `Page`/`PageRequest`.
  - O `hibernate-processor`, no `maven-compiler-plugin`, gera a implementação e **valida o HQL na compilação**.
  - O `select new` deve usar records planos, porque o Hibernate não instancia records aninhados.
- **Por quê:** campo errado vira erro de compilação. Repository sem código escrito à mão.
- **Atenção:** é `StatelessSession`.
  - Não há cache, cascade nem dirty checking: toda gravação é explícita.
  - Leitura fora de transação, fora do HTTP, exige `@ActivateRequestContext`.

### 2.18 Flyway, sem editar migration aplicada

- **O quê:**
  - `quarkus.flyway.migrate-at-start=true` e `quarkus.hibernate-orm.schema-management.strategy=validate`;
  - migrations em `src/main/resources/db/migration`;
  - mudança de schema é sempre uma migration nova;
  - colunas com nome explícito (`@Column(name = "DATA_ABERTURA")`).

### 2.19 Testes em três níveis, separados pelo nome

- **Testes puros**, sem Quarkus: regras, estados, value objects, integração com o cliente em lambda e ArchUnit.
  Rodam com `mvn test -Prapido`, sem Docker (o profile `rapido` está no `pom.xml` de `MODELOS.md`).
- **`@QuarkusTest`, com nome terminando em `BancoTest`**: motor e repositories com o Oracle do Dev Services e os sistemas externos
  falsos. Todos usam **o mesmo conjunto** de `@WithTestResource`; um conjunto diferente reinicia o app.
- **`@QuarkusIntegrationTest` (`*IT`)**: contra o jar, pela API; parametrizado por tipo; com um cenário de
  concorrência.
- **Não há fakes** dos repositories: o banco real roda nos `@QuarkusTest`.
- **Por que nome e não tag do JUnit:** o Quarkus sobe o Dev Services já ao *descobrir* uma classe `@QuarkusTest`, antes
  de qualquer filtro por tag; então `-DexcludedGroups` não evita o Docker. O surefire filtra pelo nome antes da
  descoberta. O `ArquiteturaTest.testesPeloNome` garante a convenção.

### 2.20 Cobertura com três fontes

O relatório do JaCoCo une três fontes em `target/jacoco-merged.exec`:

- **testes puros**, pelo agente do JaCoCo, com `exclClassLoaders=*QuarkusClassLoader`;
- **`@QuarkusTest`**, pela extensão `quarkus-jacoco`, que instrumenta antes das transformações do Quarkus;
- **ITs**, pelo agente no jar via `quarkus.test.arg-line=${jacoco.agent.it}` no failsafe.

O relatório sai em `target/site/jacoco/jacoco.xml`, que é o que o Sonar lê. A configuração completa está no
`pom.xml` em `MODELOS.md`.

**Limite:** classe que o Quarkus transforma no build (bean `@ApplicationScoped` sem construtor vazio, entidade) e que
só os ITs exercitam aparece subestimada. Cubra com `@QuarkusTest`, se o gate exigir.

---

## 3. Estrutura alvo

```
<pacote-base>
  apresentacao/          só traduz: pedido → modelo, chama o negócio, resultado → resposta
    rest/                *Resource, TratadorDeErros
      dto/               *Request (com paraModelo()), *Response (com de(...)), Problema, PaginaResponse
    mensageria/          consumidores de fila
      dto/               mensagens no formato do canal
  negocio/               a lógica do sistema
    <nucleo>/            o motor (*Service), o ponto de extensão, RegrasComuns, Especificacao, exceções
    <tipo>/              uma pasta por tipo: Modalidade<Tipo> + Regras<Tipo>
    <cadastro>/          CRUD simples: <Nome>Service + exceção de não encontrado
  integracao/            a lógica de sistemas externos
    <sistema>/           *Client, *ClientSimulado, formato externo, *Integracao
  persistencia/          a lógica de banco
    repositorio/         repositories Jakarta Data (escrita, compare-and-set, idempotência) + entidades técnicas
    consulta/            *Consultas + projeções (records planos) para as telas
  modelo/                o vocabulário comum: entidades, enums de estado, value objects, exceções comuns
```

| Camada | Pode usar | Nunca usa |
|---|---|---|
| `apresentacao` | `negocio`, `persistencia.consulta`, `modelo` | `persistencia.repositorio`, `integracao`, outro canal, JPA, transação |
| `negocio` | `integracao`, `persistencia`, `modelo` | `apresentacao`; um tipo não usa outro; JPA |
| `integracao` | `modelo` | as outras camadas; um sistema não usa outro; JPA |
| `persistencia` | `modelo` | as outras camadas |
| `modelo` | JPA (só as anotações) | qualquer camada; REST, CDI, Quarkus |

**Nomes pelo papel:**

- `*Resource`, `*Request`, `*Response` na apresentação;
- `*Service` e `Modalidade*` no negócio;
- `*Client` e `*Integracao` na integração;
- `*Repository` e `*Consultas` na persistência.

Classes, métodos e mensagens em português; pastas estruturais também. Siga o idioma que o projeto alvo já usa.

---

## 4. Modelos de código

Estão em **`MODELOS.md`**, com os arquivos completos do projeto de referência: entidades, State, value objects,
`Especificacao`, motor, ponto de extensão, repositories, consultas, integração, resources, tratador de erros,
consumidor de fila, CRUD, `pom.xml`, `application.properties` e a infraestrutura de teste.

Adapte nomes e regras ao domínio do projeto alvo, mas mantenha a forma.

---

## 5. Configuração

- **`pom.xml`:** copie o build e as dependências do `pom.xml` de `MODELOS.md`.
  - hibernate-processor no compilador;
  - failsafe com `quarkus.test.arg-line`;
  - JaCoCo com as quatro execuções (prepare-agent, prepare-agent-integration, merge, report);
  - `quarkus-jacoco` em teste;
  - ArchUnit `archunit`, não o `archunit-junit5`;
  - `smallrye-reactive-messaging-in-memory` se houver fila;
  - o profile `rapido`, que exclui `**/*BancoTest.java`;
  - propriedades do Sonar (`sonar.java.source` é obrigatória).
- **`application.properties`:** veja `MODELOS.md`.
  - **Nunca** ponha URL do banco, `amqp-host` ou credenciais: isso desliga o Dev Services e vaza ambiente. Em
    produção, tudo vem por variável de ambiente.
- **ArchUnit:** `ArquiteturaTest.java` desta pasta, com `src/test/resources/archunit.properties` durante a migração.

---

## 6. Roteiro de migração

Regras gerais, que valem para todas as fases:

- **Uma fase por vez.** Ao fim de cada fase:
  - `mvn verify` verde;
  - commit com a mensagem `Migração camadas — fase N: <resumo>`;
  - um resumo para a pessoa revisar antes da próxima.
- **Nunca mude o comportamento externo durante a migração.** Os ITs da Fase 1 não são editados depois de prontos.
  Se um deles quebrar, o erro está na migração.
- **Passos pequenos.** Mova primeiro e mude depois, separando estrutura de comportamento (Beck, *Tidy First?*). Use
  `git mv` para manter o histórico.
- **Na dúvida sobre regra de negócio, pergunte.**

### Fase 0 — Diagnóstico (não muda código)

Mapeie e escreva `docs/migracao/diagnostico.md` com:

1. **O que existe:**
   - módulos Maven;
   - pacotes;
   - entidades e herança;
   - serviços;
   - endpoints, filas e jobs;
   - clientes de sistemas externos;
   - onde há `@Transactional`;
   - repositories (Panache? `EntityManager`? Jakarta Data?);
   - testes existentes, por tipo;
   - migrations.
2. **Uma tabela "de → para"**: cada classe e a camada e o pacote de destino, com o papel (`*Resource`, `*Service`…).
3. **Os padrões que se aplicam** (seção 2), cada um com "aplica / não aplica / já existe" e o porquê.
4. **Desvios encontrados**, usando a tabela da seção 7.
5. **Riscos e perguntas** para a pessoa.

**PARE aqui e peça revisão.**

### Fase 1 — Rede de segurança

1. **ITs de ponta a ponta** (`@QuarkusIntegrationTest`) para a API atual. Reaproveite os que existem e crie os que
   faltam, para cada endpoint e fila: caminho feliz, um erro de regra, não encontrado. A partir daqui eles não mudam.
2. **OpenAPI de referência:**
   - rode `mvn package -DskipTests -Dquarkus.smallrye-openapi.store-schema-directory=docs/migracao/openapi-antes`;
   - se a extensão de OpenAPI não estiver no projeto, acrescente `quarkus-smallrye-openapi`.
3. **`AGENTS.md`** desta pasta na raiz, preenchido com os nomes reais. Copie também `copilot-instructions.md` para
   `.github/`.
4. **`ArquiteturaTest.java`:**
   - em `src/test/java/<pacote-base>`, com `MIGRACAO = true`;
   - `archunit.properties` em `src/test/resources`;
   - rode uma vez para criar o `archunit_store` e faça commit dele.
5. **Dependências de teste:** `archunit` e, se houver fila, `smallrye-reactive-messaging-in-memory`.

### Fase 2 — Build

- Se o projeto tem vários módulos: um módulo só.
  - Mova `src/` de cada módulo para a raiz com `git mv`.
  - Junte as dependências.
  - Tire o Jandex e os test-jars, que já não são necessários.
- Configure o hibernate-processor, se for usar Jakarta Data (Fase 3), e o JaCoCo como em `MODELOS.md`.
- Sem mudar nenhuma classe de lugar ainda. Verde e commit.

### Fase 3 — Camadas, uma de cada vez

Ordem: **modelo → persistência → integração → negócio → apresentação**. Em cada subetapa:

- mova as classes com `git mv`, ajuste pacotes e imports, `mvn verify` verde e commit;
- veja se a regra correspondente do `ArquiteturaTest` ficou sem violações congeladas;
- quando ficar, ela já está valendo. O store só encolhe.

1. **`modelo`:**
   - entidades (construtor de pedido, construtor `protected` vazio, getters, eventos no lugar de setters);
   - enums de estado;
   - value objects;
   - exceções que atravessam camadas (`RegraVioladaException`, `SistemaExternoIndisponivelException`,
     `TransicaoInvalidaException`).
2. **`persistencia`:**
   - repositories em `repositorio/` e consultas de tela em `consulta/` (projeções);
   - se o projeto usa Panache ou `EntityManager`, **não troque tudo de uma vez** (veja a seção 7);
   - entidades técnicas vão para cá.
3. **`integracao`:**
   - uma pasta por sistema, com `*Client`, `*ClientSimulado`, formato externo e `*Integracao`;
   - o negócio passa a chamar a `*Integracao`.
4. **`negocio`:**
   - serviços, regras e o motor, se houver tipos;
   - regras que estavam em resource, DTO ou repository vêm para cá;
   - transações também.
5. **`apresentacao`:**
   - resources e consumidores, com requests (`paraModelo()`) e responses (`de(...)`);
   - `TratadorDeErros`.

### Fase 4 — Padrões (só onde se aplicam, conforme o diagnóstico)

Cada item é um commit:

- value objects;
- `Especificacao`;
- State em enum + CHECK;
- compare-and-set;
- idempotência por chave;
- Problem Details (cuidado: mudar o formato de erro **muda o contrato**, então pergunte antes);
- efeito externo depois do commit;
- timeouts de 2 s.

### Fase 5 — Testes e cobertura

- **Testes puros** para regras, estados, value objects e integrações.
- **`@QuarkusTest` (`*BancoTest`)** para o motor e os repositories, todos com o mesmo conjunto de
  `@WithTestResource`.
- **Cobertura** com as três fontes, conferindo `target/site/jacoco/jacoco.xml`.
- **`MIGRACAO = false`.** Apague `archunit_store` e `archunit.properties`. Tudo verde.

### Fase 6 — Documentação

- `AGENTS.md` final, sem marcadores.
- ADRs em `docs/adr/` (modelo: contexto, decisão, consequências). Uma por decisão da seção 2 que o projeto adotou.
  Pelo menos: camadas num módulo, entidade como modelo, transações, Problem Details.
- README: como rodar e a tabela da API.

### Critério de pronto da migração

- [ ] `mvn verify` verde.
- [ ] `ArquiteturaTest` com `MIGRACAO = false`, sem store.
- [ ] Os ITs da Fase 1 continuam iguais e verdes.
- [ ] O OpenAPI gerado agora, comparado ao de `docs/migracao/openapi-antes`:
  - mesmos caminhos, campos e valores;
  - diferenças só em nomes de schema, explicadas no resumo.
- [ ] Toda classe de produção numa camada; nenhuma interface com uma implementação só (exceto clientes HTTP).
- [ ] `AGENTS.md` e ADRs escritos.

---

## 7. Como decidir quando o código legado não se encaixa

| Situação encontrada | Saída recomendada |
|---|---|
| Regra de negócio dentro do resource ou do DTO | Mova para um `*Service` (ou para o `Regras<Tipo>`); o resource só traduz. |
| `@Transactional` no resource | Mova para o serviço do negócio. No fluxo com chamada externa, use transação curta explícita. |
| Resource devolvendo entidade | Crie um `*Response` com `de(entidade)`, ou uma projeção em `consulta/`. O JSON não pode mudar: use os mesmos nomes de campo. |
| DTO usado também como entidade (`@Entity` com anotações Jackson) | Separe: a entidade no `modelo` e o request/response na apresentação. |
| Serviço que só repassa para o repository em leituras | A apresentação chama a consulta direto (`persistencia.consulta`) e o serviço morre. |
| Serviço que chama outro serviço de outro "tipo" | Extraia o comum para o núcleo (`negocio/<nucleo>`); tipos não se enxergam. |
| Interface com uma única implementação (`FooService` + `FooServiceImpl`) | Fica só a classe; o nome sem `Impl`. |
| Panache (`PanacheEntity`, `PanacheRepository`) | Pode ficar durante a migração, desde que só em `modelo`/`persistencia`. Trocar por Jakarta Data é opcional e entra num commit por entidade, com o teste do repository antes. `PanacheEntity` com `public` fields: comece por getters, sem mudar o JSON. |
| `EntityManager` usado no serviço | Leve as queries para um repository em `persistencia`. Se usa dirty checking (alterar e não chamar save), o comportamento muda com Jakarta Data `StatelessSession`: grave explicitamente e teste. |
| Herança `SINGLE_TABLE` ou `TABLE_PER_CLASS` | Mantenha a estratégia existente; trocar exige migração de dados. Registre como decisão (ADR). |
| Enum de estado com `if`/`switch` espalhado | State em enum (`MODELOS.md`); a tabela de transições vira um teste. Se o banco tem CHECK, acompanhe numa migration nova. |
| Lock pessimista (`@Lock`, `SELECT FOR UPDATE`) | Compare-and-set, se o caso for troca de estado. Senão, mantenha e registre por quê. |
| Chamada a sistema externo dentro de transação | Tire da transação: valide antes, grave numa transação curta e chame depois do commit, se for efeito. |
| Classe utilitária usada por todas as camadas | Se é vocabulário (formatação de CPF, por exemplo), vai para `modelo` como value object. Se é técnica, vai para a camada que a usa. |
| Mapper (MapStruct ou manual) entre entidade e domínio | Elimine: a entidade é o modelo. Mapper só entre a entidade e o request/response, e de uma linha, como método do DTO. |
| Bean Validation (`@NotNull`…) no request | Pode ficar na apresentação como validação de formato, ou virar value object ou `Especificacao`. A mensagem e o status (400 ou 422) não podem mudar sem perguntar. |

---

## 8. Armadilhas conhecidas

- **`NUMBER` do Oracle perde zeros à direita** (`400000.00` vira `400000`). Compare com `compareTo`; o `Valor`
  normaliza a escala para 2.
- **`select new` não instancia record aninhado**: use projeções planas.
- **Enum inválido em `@QueryParam`** responde 404, não 400 (JAX-RS).
- **URL do banco ou `amqp-host` no `application.properties`** desligam o Dev Services, inclusive nos ITs.
- **Colunas precisam de nome explícito**: o Quarkus não converte camelCase em SNAKE_CASE.
- **Sessão stateless** (Jakarta Data): sem cascade, sem cache e sem dirty checking.
- **Discriminador só leitura**: o atributo `segmento` tem `insertable=false, updatable=false`. A filha o preenche no
  construtor de pedido, para o objeto recém-criado já ter o valor.
- **Consumidor sem request context** dá `ContextNotActiveException`: use `@ActivateRequestContext`.
- **Fila virou tópico**: sem `capabilities=queue`, o Artemis cria multicast.
- **JSON chega como texto** sem content-type: aceite `String`, `Buffer` e `byte[]` além de `JsonObject`.
- **Artemis bloqueia produtores com o disco acima de 90%.** No Dev Services, use
  `JAVA_ARGS_APPEND=-Dbrokerconfig.maxDiskUsage=-1`.
- **Imagem do Artemis**: a padrão do Dev Services vem do quay.io. Se for bloqueado, use `apache/activemq-artemis` com
  `ARTEMIS_USER`/`ARTEMIS_PASSWORD`.
- **`@QuarkusTest` com conjuntos diferentes de `@WithTestResource`** reinicia o app e o Oracle.
- **Tag do JUnit não tira `@QuarkusTest` do Docker**: o Quarkus sobe o Dev Services já ao descobrir a classe, antes
  do filtro por tag. Separe pelo nome (`*BancoTest`) e exclua no surefire (profile `rapido`).
- **Servidor falso nos ITs** roda na JVM do teste; o mapa de `start()` vira configuração do jar.
- **ArchUnit**: use a API core (`check`) dentro de `@Test`. O engine `archunit-junit5` não roda sob o JUnit 6 do BOM
  e executaria 0 testes.
- **Subclasse com construtor só com argumentos e `@ApplicationScoped`**: o ArC precisa gerar um construtor vazio. Se a
  classe base também não tiver um, use `@Singleton`. Isso não acontece com interface + implementação direta.
- **Enhancement do Hibernate acrescenta campos `$$_hibernate_*`** às entidades. Reflexão sobre campos precisa
  ignorá-los, filtrando os estáticos, os transientes, os sintéticos e os que começam com `$$`.
- **`Unsatisfied dependency` de um bean que existe**, num projeto multi-módulo durante a Fase 2: falta Jandex ou o
  POM pai está desatualizado no `~/.m2`. Depois da Fase 2, isso some.
- **`hibernate-processor` e índice de entidades**: com entidades em outro módulo, o HQL deixa de ser validado sem
  aviso. Mais um motivo para um módulo só.

---

## 9. O que você (agente) nunca faz

- Mudar o contrato da API ou da fila: caminhos, campos, status, formato de erro. Se for inevitável, pare e pergunte.
- Editar uma migration já aplicada. Crie outra.
- Pular, desabilitar, afrouxar ou apagar teste para ficar verde. Isso inclui não editar os ITs da Fase 1.
- Avançar de fase com build vermelho, ou juntar fases num commit só.
- Inventar regra de negócio, mensagem ou validação que não existia.
- Pôr credenciais, URLs de ambiente ou `amqp-host` no `application.properties`.
- Criar interface com uma única implementação, mapper entre entidade e domínio, ou serviço que só repassa leitura.
- Deixar `MIGRACAO = true` ou o `archunit_store` no fim.
