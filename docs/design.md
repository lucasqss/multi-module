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

O `Processo` é o Context: guarda a `Situacao` (o valor persistido) e delega cada evento ao `EstadoDoProcesso`
correspondente. Há uma classe por estado (`Aberto`, `EmProcessamento`, `Concluido`, `Recusado`, `Cancelado`), cada
uma sobrescrevendo só as transições que permite; o `default` de todo evento lança `TransicaoInvalidaException`.

- O pacote `motor.domain.estado` é autocontido, com a própria exceção, para não formar ciclo com o domínio.
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
- os domínios só usam Java puro e o domínio do motor.

O `ArquiteturaModulosTest` do app garante que:

- core e query não se enxergam;
- nenhuma classe fora da impressão depende dela;
- ninguém depende das integrações; elas só enxergam os `domain` do core, e os pacotes de sistemas diferentes não
  se enxergam;
- ninguém depende do app;
- o `jpa-core` não conhece o resto;
- HTTP só aparece no app, na impressão e nas integrações;
- o app não usa entidades JPA nem adapters do core;
- não há ciclos entre pacotes;
- toda classe pertence a um módulo registrado.

## Core (`com.example.processo.core`)

**Motor (`core.motor`)**

- `domain`, Java puro:
  - `Processo<D>`, `Solicitacao<D>`, `Proponente`, `DadosDoSegmento`;
  - `RegrasComuns`: nome obrigatório, CPF com dígitos verificadores, valor > 0;
  - `Especificacao`, `RegraVioladaException`;
  - ports `Segmento<D>`, `Numeracao` e `SituacaoCadastral` (sistema externo, implementado em `integracoes`);
  - evento `ProcessoCriado`.
- `application`: `MotorDeProcessamento`, `@Transactional`. Recusa dois segmentos com o mesmo código ou o mesmo
  tipo de dados.
- `adapter`: `Relogio`, o `Clock` injetado, e `persistence.NumeracaoJpa`, com `@SQL` na sequence.

**Segmento imobiliário (`core.segmento.imobiliario`)**

- `domain`:
  - `DadosImobiliarios(matricula, tipoImovel, valorAvaliacao, uf)`;
  - `TipoImovel`, com o limite da avaliação: residencial 80%, comercial 60%;
  - `RegrasImobiliarias`;
  - port `ProcessosImobiliarios`.
- `application.SegmentoImobiliario`, o bean que pluga o segmento no motor. Por último aplica a regra que consulta
  o banco: não pode haver outro processo aberto para a mesma matrícula na mesma UF.
- `adapter.persistence.ProcessosImobiliariosJpa`:
  - `@Insert` da `ProcessoImobiliarioEntity` grava as duas tabelas;
  - a contagem por matrícula filtra pela `situacao`, que está no pai, e o Hibernate faz o join sozinho.

## Query (`com.example.processo.query`)

`ProcessoQueries` (`@Repository`):

- `todos()` e `naSituacao(situacao)` devolvem `ResumoProcesso`;
- `imobiliario(id)` devolve `DetalheImobiliario`.

As projeções são records top-level e planos, porque o `select new` não instancia records aninhados.

## App (`com.example.processo.app.rest`)

- `ProcessoImobiliarioResource`: `POST /processos/imobiliarios` cria pelo motor; `GET /processos/imobiliarios/{id}`
  lê pelo query.
- `ProcessosResource`: `GET /processos?situacao=` faz a listagem.
- `Criado`: o corpo do 201.
- `ErrosMapper`: traduz `RegraVioladaException` em 422.

## Integrações (`com.example.processo.integracoes`)

O pacote `cadastro` tem quatro classes:

- `CadastroClient`: o REST Client do sistema externo;
- `CadastroClientSimulado`: a simulação do perfil dev, que responde sempre `REGULAR`;
- `RespostaCadastro`: o modelo externo;
- `SituacaoCadastralAdapter`: implementa o port `SituacaoCadastral`.

A simulação ocupa o lugar do REST Client sem que nenhum outro código saiba:

- `@IfBuildProfile("dev")` faz o bean existir só no build de dev;
- `@RestClient` dá a ele o mesmo qualificador do REST Client gerado;
- `@Alternative @Priority(1)` faz o ArC preferi-lo ao REST Client.

No jar de produção o bean não existe, então só sobra o REST Client. Os ITs rodam esse jar contra um servidor
HTTP falso (`CadastroFalso`), e assim a chamada real é testada: a resposta `SUSPENSA` para um CPF só pode vir
de lá, porque a simulação responde sempre `REGULAR`.

## Impressão (`com.example.processo.impressao`)

- `ImpressaoDeProcessos`: o observer do evento.
- `SistemaDePdf`: o REST Client, `POST /documentos`, com o endereço em `quarkus.rest-client.sistema-pdf.url`.
- `Documento(modelo, referencia, processo)`: o modelo é `abertura-processo-<segmento>`, a referência é o número
  do processo e `processo.dados` leva os campos do segmento.

## Testes

| Onde | O quê | Docker |
|---|---|---|
| `processo-core` | regras comuns e CPF; motor com dois segmentos falsos (mesmo fluxo, numeração única, regra violada não grava nem publica, situação cadastral irregular, sistema externo só consultado no fim); segmento imobiliário no motor real com port em memória; ArchUnit motor x segmentos | não |
| `integracoes` | tradução da resposta externa no adapter | não |
| `impressao` | montagem do `Documento` | não |
| `processo-app` | ArchUnit entre módulos; `@QuarkusIntegrationTest` contra o jar, com Oracle e um sistema de PDF falso (`HttpServer` do JDK): criação, recusa por matrícula, numeração, regras, falha do PDF, detalhe e listagem | sim |

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
