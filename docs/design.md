# Design — Processos com motor único (Quarkus 3.33 · Java 25 · Oracle)

## Objetivo

Exemplo mínimo, com **dois tipos de processo (imobiliário e veicular)** criados, processados, cancelados e
consultados pelo mesmo motor, para validar seis requisitos:

1. **O processo de cada tipo é filho do processo.** Os dados comuns ficam no pai e os do tipo na filha.
2. **O motor de processamento é único para todos os segmentos.** Segmento novo não altera o motor.
3. **A impressão (ponte para o sistema de PDF) é descartável.** Ela fica num módulo próprio que pode ser apagado.
4. **Toda a lógica de negócio fica num `processo-core`, sem HTTP.** O REST fica no app e as consultas num módulo
   à parte.
5. **Integrações de saída não contaminam o core.** O core declara o que precisa (port) e o módulo `integracoes`
   implementa.
6. **A máquina de estados é única e usa o padrão State (GoF).** Os tipos compartilham o ciclo de vida
   `ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO`, e `ABERTO → CANCELADO`.

## Decisões

| Tema | Decisão |
|---|---|
| Módulos | `jpa-core`, `processo-core`, `processo-query`, `integracoes`, `impressao`, `processo-app` |
| Integrações de saída | Port no domínio do core, implementação em `integracoes.<sistema>` (um pacote por sistema), com tradução do modelo externo; o app liga pela dependência |
| Motor e segmentos | No mesmo módulo (`processo-core`), em pacotes `motor` e `segmento.<nome>`; ArchUnit garante que o motor não conhece segmentos e que segmentos não se enxergam |
| REST | Todo no `processo-app`; o core não tem dependência de HTTP |
| Consultas | `processo-query`: Jakarta Data somente leitura com projeções `select new`; não depende do core |
| Pai e filha no banco | Herança JPA `JOINED`: `PROCESSO` (comum, com discriminador `SEGMENTO`) e `PROCESSO_IMOBILIARIO` (PK = FK para o pai) |
| Pai e filha no domínio | `Processo<D extends DadosDoSegmento>`: um processo imobiliário é um `Processo<DadosImobiliarios>` |
| Motor único | `MotorDeProcessamento` descobre os beans `Segmento<?>` (`@Any Instance`) e escolhe pela classe dos dados |
| Ponto de extensão | SPI `Segmento<D>`: código, prefixo, tipo dos dados, regras próprias e gravação |
| Regras | `Especificacao<T>`: devolve o motivo da violação; comuns primeiro, depois as do segmento, por último a que consulta o banco |
| Numeração | Sequence `SEQ_NUMERO_PROCESSO`, única para todos os segmentos: `<prefixo>-<ano>-<6 dígitos>` |
| Impressão | Observa `ProcessoCriado` com `TransactionPhase.AFTER_SUCCESS` e chama o sistema de PDF por REST Client |
| Persistência | Jakarta Data sobre `StatelessSession`, HQL validado em compilação pelo Hibernate Processor |
| Deploy | Único, via `processo-app` |
| Testes | Unitários sem Quarkus (motor com segmentos falsos; segmento plugado no motor real com fake do port); ArchUnit no core e entre módulos; `@QuarkusIntegrationTest` contra o jar com Oracle do Dev Services e um sistema de PDF falso |

### Máquina de estados com o padrão State

O `Processo` é o Context: guarda a `Situacao` e delega cada evento a ela. O State é o próprio `enum Situacao`, e
cada constante é um estado concreto que sobrescreve só as transições que permite; por padrão todo evento lança
`TransicaoInvalidaException`. Um enum em vez de uma classe por estado: é o mesmo padrão, com a máquina inteira num
arquivo, e o valor persistido e o comportamento são o mesmo objeto.

- O pacote `motor.domain.state` é autocontido, com a própria exceção, para não formar ciclo com `domain.model`.
- O processamento tem duas transações curtas, com a avaliação do tipo fora delas. A troca de situação é
  compare-and-set, com update nativo em `PROCESSO` condicionado à situação anterior: um update HQL numa hierarquia
  JOINED usaria a estratégia multi-tabela do Hibernate. Conflito responde 409.
- Os tipos só decidem (`Segmento.avaliar`). Estados, transições e persistência da situação são do motor.
- Um tipo que precise de estados ou etapas que os outros não têm deve ganhar fluxo próprio, em vez de estender o
  SPI com exceções.

### Por que motor e segmentos no mesmo módulo

Tudo vai no mesmo jar e é mantido pelo mesmo time. Um módulo Maven por segmento daria a mesma garantia que as
regras do ArchUnit já dão, mas cobraria mais um POM, mais um jandex e mais uma dependência direta no app por
segmento. Um módulo por segmento só compensaria com times diferentes, dependências pesadas exclusivas de um
segmento ou deploys com conjuntos diferentes de segmentos. A impressão continua em módulo próprio porque ali o
módulo tem função: apagar a pasta remove a ponte.

### Por que o REST fica no app

O core fica só com regra e persistência, e não sabe que existe HTTP (o Maven garante: ele não tem a extensão
REST). O app é a porta de entrada: traduz HTTP, cria pelo motor e lê pelo query. Outras entradas, como um
consumidor de fila do mainframe, entram no mesmo lugar ou, se forem descartáveis, num módulo próprio como a
impressão. Custo: segmento novo também ganha código no app (os endpoints).

### Por que integrações num módulo à parte, e não no app nem no core

- **No core**, a integração traria HTTP e os modelos dos sistemas externos para dentro da regra de negócio.
- **No app**, ele viraria depósito de entrada e saída, juntando as dependências e a configuração de todos os
  sistemas, e remover uma integração viraria cirurgia.
- **Em `integracoes`**, o core declara o port (`SituacaoCadastral`) e testa com fakes, e o módulo implementa e
  traduz. Um pacote por sistema mantém um módulo só enquanto os sistemas forem pequenos e estáveis.

Sistema com ciclo de vida próprio, dependências pesadas (SOAP, MQ, código gerado) ou descartável ganha módulo
próprio. O motor consulta o sistema externo por último, só quando a solicitação já passou nas outras regras.

### Por que um módulo de consultas

A leitura não precisa de domínio nem de regras, e o domínio não deve ganhar getters só para alimentar telas.
O `processo-query` lê direto as entidades do `jpa-core` com projeções. Com a herança `JOINED`:

- **Listagem de todos os segmentos:** consulta sobre o pai, `ProcessoEntity`, projetando só colunas de
  `PROCESSO`. O discriminador está mapeado como atributo somente leitura (`segmento`), então a listagem diz o
  segmento sem conhecer as filhas.
- **Detalhe de um segmento:** consulta direto a filha, que já enxerga os campos herdados.

### Por que herança no banco e genéricos no domínio

O domínio usa records imutáveis, e record não estende classe. Além disso, um `sealed` não serve entre módulos
Maven: as subclasses permitidas precisam estar no mesmo pacote e ser compiladas juntas. Por isso o domínio
compõe: `Processo<D>` carrega os dados comuns, e `D` são os do segmento. Para o motor, todo processo é igual e só
`D` muda. No banco, a mesma separação vira herança `JOINED`, que é o que dá as tabelas pai e filha. Cada
segmento converte o próprio `Processo<D>` na própria entidade filha.

### Por que um endpoint por segmento

O corpo da requisição é do segmento. Um `POST /processos` único exigiria que o motor ou o JSON polimórfico
conhecessem todos os subtipos, e isso quebra a regra de que segmento novo não toca no motor. Todos os
endpoints entregam a solicitação ao mesmo `MotorDeProcessamento.criar`. A listagem, por ser só dos campos
comuns, é uma só: `GET /processos`.

### Por que evento e não port para a impressão

Com um port `Impressora` no motor, o motor dependeria de alguém implementá-lo, e remover o módulo exigiria um
substituto. Com o evento, o motor só publica `ProcessoCriado` e não sabe se alguém escuta. `AFTER_SUCCESS`
garante que só se imprime o que foi gravado e que uma falha no sistema de PDF não desfaz o processo.

A chamada é síncrona, depois do commit e antes da resposta HTTP. Em produção, se o PDF não puder se perder,
o próximo passo é uma tabela de saída (outbox) com reprocessamento, ainda dentro do módulo `impressao`.

## Estrutura e dependências

```
multi-module/
├─ pom.xml            agregador: BOM Quarkus, versões internas, hibernate-processor, jandex, failsafe, JaCoCo, Sonar
├─ docker-compose.yml Oracle para rodar o jar empacotado
├─ jpa-core/          @Entity pai/filha + db/migration     → quarkus-hibernate-orm, quarkus-flyway(-oracle)
├─ processo-core/     motor + segmento.*                   → jpa-core, jakarta.data-api
├─ processo-query/    consultas somente leitura            → jpa-core, jakarta.data-api
├─ integracoes/       um pacote por sistema externo        → processo-core
├─ impressao/         observer + REST Client               → processo-core, quarkus-rest-client-jackson
└─ processo-app/      REST + montagem + ITs + ArchUnit     → processo-core, processo-query, integracoes, impressao, jpa-core, quarkus-rest-jackson, quarkus-jdbc-oracle
```

O `ArquiteturaTest` do core garante que:

- o motor não conhece os segmentos;
- os segmentos não se enxergam;
- as camadas seguem `adapter/config → application → domain`: o domínio só usa Java puro (e o domínio do motor),
  e a application só conhece os ports, nunca os adapters;

O `ArquiteturaModulosTest` do app garante que:

- core e query não se enxergam;
- nenhuma classe fora da impressão depende dela;
- ninguém depende das integrações; elas só enxergam o domínio e os `port.out` do core, e os pacotes de sistemas
  diferentes não se enxergam;
- ninguém depende do app;
- o `jpa-core` não conhece o resto;
- HTTP só aparece no app, na impressão e nas integrações;
- o app entra no core pelo motor (`application.service`) e pelos tipos do domínio, nunca pelos ports de saída, SPI ou adapters; não usa entidades JPA;
- no REST, o `dto` não conhece `resource` nem `mapper`, e o `mapper` não conhece `resource`;
- não há ciclos entre pacotes;
- toda classe pertence a um módulo registrado.

## Convenção de pastas

Estrutura hexagonal, igual em todo contexto. As pastas estruturais têm nomes em inglês e as classes, em português.

| Pasta | O que vai lá |
|---|---|
| `domain/model` | entidades e value objects do domínio (records), sem framework |
| `domain/state` | a máquina de estados (padrão State) |
| `domain/rule` | `Especificacao` e as regras |
| `domain/exception` | exceções de negócio |
| `domain/event` | eventos de domínio |
| `application/port/out` | o que a aplicação precisa de fora (persistência, sistemas externos, transação) |
| `application/port/spi` | ponto de extensão do motor (`Segmento`) |
| `application/service` | o motor e as implementações do SPI (os segmentos) |
| `adapter/inbound/rest` | `resource/` (endpoints), `dto/` (contratos de request/response, com a conversão de/para o domínio), `exception/` (exceção → HTTP) |
| `adapter/outbound/persistence` | `repository/` (Jakarta Data, implementa o port de persistência) e `mapper/` (entidade ↔ domínio) |
| `config` | producers CDI (ex.: `Clock`) |

### Interface só onde há costura real

Uma interface só existe quando há uma costura de verdade: entre módulos, com sistema externo ou num ponto de
extensão. Os exemplos são o SPI `Segmento` (o motor não conhece os segmentos), `SituacaoCadastral` (o core não conhece o
REST Client, que mora em `integracoes`) e o evento `ProcessoCriado` (a impressão pode ser apagada). Dentro de um
contexto, injeta-se a classe: uma interface com uma única implementação no mesmo módulo é indireção sem retorno. Por
isso o REST injeta o `MotorDeProcessamento` direto, e o repository Jakarta Data implementa o port de persistência
sem uma classe adapter no meio.

Na mesma linha, **mapper só quando a conversão tem lógica**. Conversão de uma linha é método do próprio DTO
(`request.paraSolicitacao()`, `ProcessoCriadoResponse.de(p)`, `resposta.regular()`, `Documento.deAbertura(p)`).
Classe em `mapper/` fica para domínio ↔ entidade JPA, que traduz enums e monta pai e filha.

## Core (`com.example.processo.core`)

**Motor (`core.motor`)**

- `domain`, Java puro:
  - `model`: `Processo<D>` (Context do State), `Solicitacao<D>`, `Proponente`, `DadosDoSegmento`, `Avaliacao`;
  - `state`: `Situacao` (o State, um enum) e `TransicaoInvalidaException`;
  - `rule`: `Especificacao`, `RegrasComuns` (nome obrigatório, CPF com dígitos verificadores, valor > 0);
  - `exception`: regra violada, processo não encontrado, conflito de situação, sistema externo indisponível;
  - `event`: `ProcessoCriado`.
- `application`:
  - `port/out`: `Processos`, `Numeracao`, `SituacaoCadastral` (implementado em `integracoes`), `Transacoes`;
  - `port/spi`: `Segmento<D>`;
  - `service`: `MotorDeProcessamento`, o ponto de entrada do core (criar, processar, cancelar). Recusa dois
    segmentos com o mesmo código ou o mesmo tipo de dados.
- `adapter/outbound`:
  - `persistence/repository`: `ProcessoRepository` (update condicional com `@SQL`) e `NumeracaoRepository`
    (sequence), que implementam os ports `Processos` e `Numeracao`;
  - `transaction`: `TransacoesQuarkus`.
- `config`: `Relogio`, o producer do `Clock`.

**Segmentos (`core.segmento.imobiliario`, `core.segmento.veicular`)**

- `domain/model`: `DadosImobiliarios` (com `TipoImovel` e o limite da avaliação: residencial 80%, comercial 60%)
  ou `DadosVeiculares`.
- `domain/rule`: `RegrasImobiliarias` ou `RegrasVeiculares`.
- `application/port/out`: `ProcessosImobiliarios` ou `ProcessosVeiculares`.
- `application/service`: `SegmentoImobiliario` ou `SegmentoVeicular`, o bean que implementa o SPI `Segmento`. O
  imobiliário aplica por último a regra que consulta o banco: não pode haver processo em andamento para a mesma
  matrícula na mesma UF.
- `adapter/outbound/persistence`:
  - `repository/` (`ProcessoImobiliarioRepository`) implementa o port: o `@Insert` da entidade filha grava as duas
    tabelas, o `@Find` lê, e os métodos `default` do port convertem pelo mapper;
  - `mapper/` (`ProcessoImobiliarioMapper`) converte entidade ↔ domínio.

## Query (`com.example.processo.query`)

- `repository/ProcessoQueries` (`@Repository`): `todos()` e `naSituacao(situacao)` devolvem `ResumoProcesso`;
  `imobiliario(id)` e `veicular(id)` devolvem o detalhe de cada tipo.
- `dto/`: as projeções `ResumoProcesso`, `DetalheImobiliario` e `DetalheVeicular`. São records top-level e planos,
  porque o `select new` não instancia records aninhados.

## App (`com.example.processo.app.adapter.inbound.rest`)

- `resource/`: `ProcessoImobiliarioResource` e `ProcessoVeicularResource` (criar pelo motor, detalhe pelo query) e
  `ProcessosResource` (listagem pelo query; processar e cancelar pelo motor).
- `dto/`: contratos de request (`NovoProcessoImobiliarioRequest`, `ImovelRequest`, `NovoProcessoVeicularRequest`,
  `VeiculoRequest`, `ProponenteRequest`) e de response (`ProcessoCriadoResponse`, `SituacaoResponse`,
  `ErroResponse`). Os requests convertem a si mesmos em `Solicitacao` (`paraSolicitacao()`) e os responses se
  montam do processo (`de(p)`). Os GETs devolvem direto as projeções do query, que já são o contrato de leitura.
- `exception/TratadorDeErros`: exceções do domínio → 422, 404, 409 e 503.

## Integrações (`com.example.processo.integracoes`)

O pacote `cadastro` tem três subpacotes:

- `client/`: `CadastroClient`, o REST Client do sistema externo, e `CadastroClientSimulado`, a simulação do perfil
  dev, que responde sempre `REGULAR`;
- `dto/`: `RespostaCadastro`, o modelo externo, com `regular()`, que converte a resposta para o que o domínio
  entende;
- `adapter/`: `SituacaoCadastralAdapter`, que implementa o port `SituacaoCadastral`.

A simulação ocupa o lugar do REST Client sem que nenhum outro código saiba:

- `@IfBuildProfile("dev")` faz o bean existir só no build de dev;
- `@RestClient` dá a ele o mesmo qualificador do REST Client gerado;
- `@Alternative @Priority(1)` faz o ArC preferi-lo ao REST Client.

No jar de produção o bean não existe, então só sobra o REST Client. Os ITs rodam esse jar contra um servidor
HTTP falso (`CadastroFalso`), e assim a chamada real é testada: a resposta `SUSPENSA` para um CPF só pode vir
de lá, porque a simulação responde sempre `REGULAR`.

## Impressão (`com.example.processo.impressao`)

- `listener/ImpressaoDeProcessos`: o observer do evento.
- `client/SistemaDePdf`: o REST Client, `POST /documentos`, com o endereço em `quarkus.rest-client.sistema-pdf.url`.
- `dto/Documento(modelo, referencia, processo)`. `Documento.deAbertura(p)` escolhe o modelo
  `abertura-processo-<segmento>`, usa o número do processo como referência, e `processo.dados` leva os campos do
  segmento.

## Testes

| Onde | O quê | Docker |
|---|---|---|
| `processo-core` | tabela do State; regras comuns e CPF; motor com dois segmentos falsos (mesmo fluxo e ciclo de vida, numeração única, regra violada não grava nem publica, situação cadastral irregular, conflito, sistema externo só consultado no fim); segmentos imobiliário e veicular no motor real; ArchUnit de camadas e motor x segmentos | não |
| `integracoes` | adapter e conversão da resposta externa; falha do cliente vira indisponibilidade | não |
| `impressao` | `Documento.deAbertura` | não |
| `processo-app` | ArchUnit entre módulos; `@QuarkusIntegrationTest` contra o jar, com Oracle e um sistema de PDF falso (`HttpServer` do JDK): ciclo de vida igual por tipo (inclusive processamento simultâneo), recusa por matrícula, numeração, regras, cadastro irregular e fora do ar, falha do PDF, detalhe e listagem | sim |

## Armadilhas

Das herdadas do exemplo da biblioteca, valem estas:

1. **O índice de entidades precisa ir no jar.** Sem o `copy-resources` do `entity/index` no `jpa-core`, o HQL que
   cita entidades dele deixa de ser validado nos outros módulos, sem aviso.
2. **Sessão stateless:** não faz cascade nem tem cache de 1º nível. Toda gravação é explícita.
3. **Nomes de coluna explícitos.** O Quarkus não converte `dataAbertura` em `DATA_ABERTURA` por padrão.
4. **ArchUnit com JUnit 6.** O engine `archunit-junit5` não executa sob o JUnit 6 do BOM. A solução é usar a API
   core dentro de um `@Test` comum.
5. **`%prod` x ITs.** Configurar a URL do banco em `%prod` desliga o Dev Services nos `@QuarkusIntegrationTest`.

Novas:

6. **`@Any Instance<Segmento<?>>`** encontra todo bean que implementa `Segmento<X>`, e o ArC não remove esses beans
   como "não usados". Não é preciso registrar o segmento em lugar nenhum além do classpath.
7. **Sistema externo nos `@QuarkusIntegrationTest`.** O test resource (`@WithTestResource`) roda na JVM do teste,
   e o mapa que `start()` devolve vira configuração do jar, que roda em outro processo. Assim o jar chama o
   servidor falso, e o teste lê o que o servidor recebeu.
8. **`NUMBER` do Oracle não guarda zeros à direita.** `400000.00` volta do banco como `400000`, e é assim que sai
   no JSON. Nos testes, compare valores numericamente. Se a API precisar de duas casas fixas, formate na borda.
9. **`@QueryParam` com enum inválido responde 404**, não 400, como manda o JAX-RS para parâmetros que não
   convertem.
10. **Simulação de sistema externo por perfil.** Um bean `@IfBuildProfile("dev") @Alternative @Priority(1)
    @RestClient` que implementa a interface do REST Client substitui o cliente gerado só no dev. A condição é de
    build: o `quarkus:dev` monta com o perfil dev, e o jar (produção e ITs) com o prod.
