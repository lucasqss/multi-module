# Design — Criar processo imobiliário (Quarkus 3.33 · Java 25 · Oracle)

## Objetivo

Exemplo mínimo, só com o **criar processo imobiliário**, para validar três requisitos:

1. **Processo imobiliário é filho do processo.** Os dados comuns ficam no pai e os do imóvel na filha.
2. **O motor de processamento é único para todos os segmentos.** Segmento novo não altera o motor.
3. **A impressão (ponte para o sistema de PDF) é descartável.** Ela fica num módulo próprio que pode ser apagado.

## Decisões

| Tema | Decisão |
|---|---|
| Módulos | `jpa-core`, `processo-motor`, `segmento-imobiliario`, `impressao`, `processo-app` |
| Pai e filha no banco | Herança JPA `JOINED`: `PROCESSO` (comum, com discriminador `SEGMENTO`) e `PROCESSO_IMOBILIARIO` (PK = FK para o pai) |
| Pai e filha no domínio | `Processo<D extends DadosDoSegmento>`: um processo imobiliário é um `Processo<DadosImobiliarios>` |
| Motor único | `MotorDeProcessamento` descobre os beans `Segmento<?>` (`@Any Instance`) e escolhe pela classe dos dados |
| Ponto de extensão | SPI `Segmento<D>`: código, prefixo, tipo dos dados, regras próprias e gravação |
| Regras | `Especificacao<T>`: devolve o motivo da violação; comuns primeiro, depois as do segmento, por último a que consulta o banco |
| Numeração | Sequence `SEQ_NUMERO_PROCESSO`, única para todos os segmentos: `<prefixo>-<ano>-<6 dígitos>` |
| Impressão | Observa `ProcessoCriado` com `TransactionPhase.AFTER_SUCCESS` e chama o sistema de PDF por REST Client |
| Persistência | Jakarta Data sobre `StatelessSession`, HQL validado em compilação pelo Hibernate Processor |
| Deploy | Único, via `processo-app` sem classes Java |
| Testes | Unitários sem Quarkus (motor com segmentos falsos; segmento plugado no motor real com fake do port); ArchUnit por módulo e entre módulos; `@QuarkusIntegrationTest` contra o jar com Oracle do Dev Services e um sistema de PDF falso |

### Por que herança no banco e genéricos no domínio

O domínio usa records imutáveis, e record não estende classe. Além disso, um `sealed` não serve entre módulos
Maven: as subclasses permitidas precisam estar no mesmo pacote e ser compiladas juntas. Por isso o domínio
compõe: `Processo<D>` carrega os dados comuns, e `D` são os do segmento. Para o motor, todo processo é igual e só
`D` muda. No banco, a mesma separação vira herança `JOINED`, que é o que dá as tabelas pai e filha. Cada
segmento converte o próprio `Processo<D>` na própria entidade filha.

### Por que um endpoint por segmento

O corpo da requisição é do segmento. Um `POST /processos` único exigiria que o motor ou o JSON polimórfico
conhecessem todos os subtipos, e isso quebra a regra de que segmento novo não toca no motor. Todos os
endpoints entregam a solicitação ao mesmo `MotorDeProcessamento.criar`.

### Por que evento e não port para a impressão

Com um port `Impressora` no motor, o motor dependeria de alguém implementá-lo, e remover o módulo exigiria um
substituto. Com o evento, o motor só publica `ProcessoCriado` e não sabe se alguém escuta. `AFTER_SUCCESS`
garante que só se imprime o que foi gravado e que uma falha no sistema de PDF não desfaz o processo.

A chamada é síncrona, depois do commit e antes da resposta HTTP. Em produção, se o PDF não puder se perder,
o próximo passo é uma tabela de saída (outbox) com reprocessamento, ainda dentro do módulo `impressao`.

## Estrutura e dependências

```
multi-module/
├─ pom.xml                 agregador: BOM Quarkus, versões internas, hibernate-processor, jandex, failsafe, JaCoCo, Sonar
├─ docker-compose.yml      Oracle para rodar o jar empacotado
├─ jpa-core/               @Entity pai/filha + db/migration      → quarkus-hibernate-orm, quarkus-flyway(-oracle)
├─ processo-motor/         domain · application · adapter        → jpa-core, jakarta.data-api, quarkus-rest-jackson
├─ segmento-imobiliario/   domain · application · adapter        → processo-motor
├─ impressao/              observer + REST Client                → processo-motor, quarkus-rest-client-jackson
└─ processo-app/           montagem + ITs + ArchUnit             → segmento-imobiliario, impressao (+ diretas p/ cobertura), quarkus-jdbc-oracle
```

O `ArquiteturaModulosTest` (no `processo-app`) garante o que o Maven não garante:

- o motor não depende de segmentos nem da impressão;
- nenhuma classe fora da impressão depende dela;
- segmentos não se enxergam;
- o `jpa-core` não conhece o resto;
- HTTP só nos adapters REST e na impressão;
- não há ciclos entre pacotes;
- toda classe pertence a um módulo registrado.

## Motor (`com.example.processo.motor`)

- `domain`, Java puro (ArchUnit):
  - `Processo<D>`, `Solicitacao<D>`, `Proponente`, `DadosDoSegmento`;
  - `RegrasComuns`: nome obrigatório, CPF com dígitos verificadores, valor > 0;
  - `Especificacao`, `RegraVioladaException`;
  - ports `Segmento<D>` e `Numeracao`;
  - evento `ProcessoCriado`.
- `application`: `MotorDeProcessamento`, `@Transactional`. Recusa dois segmentos com o mesmo código ou o mesmo
  tipo de dados.
- `adapter`:
  - `Relogio`, o `Clock` injetado;
  - `persistence.NumeracaoJpa`, com `@SQL` na sequence;
  - `rest.Criado`, o 201 comum;
  - `rest.ErrosMapper`, que traduz `RegraVioladaException` em 422.

## Segmento imobiliário (`com.example.processo.imobiliario`)

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
- `adapter.rest.ProcessoImobiliarioResource`, que expõe `POST /processos/imobiliarios`.

## Impressão (`com.example.processo.impressao`)

- `ImpressaoDeProcessos`: o observer do evento.
- `SistemaDePdf`: o REST Client, `POST /documentos`, com o endereço em `quarkus.rest-client.sistema-pdf.url`.
- `Documento(modelo, referencia, processo)`: o modelo é `abertura-processo-<segmento>`, a referência é o número
  do processo e `processo.dados` leva os campos do segmento.

## Testes

| Onde | O quê | Docker |
|---|---|---|
| `processo-motor` | regras comuns, CPF; motor com dois segmentos falsos (mesmo fluxo, numeração única, regra violada não grava nem publica); ArchUnit | não |
| `segmento-imobiliario` | segmento no motor real com port em memória: limites por tipo, matrícula aberta, dados inválidos; ArchUnit | não |
| `impressao` | montagem do `Documento` | não |
| `processo-app` | ArchUnit entre módulos; `@QuarkusIntegrationTest` contra o jar, com Oracle e um sistema de PDF falso (`HttpServer` do JDK) | sim |

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
