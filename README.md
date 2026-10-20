# Processo — motor único por segmento em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo de **processos de dois tipos (imobiliário e veicular) com um motor único**, para mostrar:

- **Um motor de processamento único** para todos os tipos (segmentos): o mesmo fluxo de criação e o mesmo ciclo de
  vida (processar, cancelar). O tipo veicular entrou sem mudar uma linha do motor. Uma regra do ArchUnit impede o
  motor de conhecer qualquer segmento.
- **Máquina de estados única com o padrão State (GoF)**: `ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO`, e
  `ABERTO → CANCELADO`. Cada situação é um objeto que sabe quais eventos aceita.
- **Processo de cada tipo como filha do processo**: herança JPA `JOINED` (tabela `PROCESSO` + uma tabela por tipo).
- **CQRS**: o `processo-core` (escrita) e o `processo-query` (leitura) não se enxergam. Os dois só se encontram
  no REST do `processo-app`.
- **Integrações de saída** no módulo `integracoes`: o core define o port e a integração o implementa, sem que
  o core conheça o sistema externo.
- **Um módulo de impressão descartável**: a ponte para o sistema de geração de PDF. Ninguém depende dele, então
  removê-lo é apagar o módulo.

## Módulos

```
                                ┌───────────────────────────┐
                                │       processo-app        │  todo o REST, montagem,
                                │   REST · ITs · ArchUnit   │  ITs e regras entre módulos
                                └─────────────┬─────────────┘
              ┌───────────────────┬───────────┴───────┬──────────────────────┐
     ┌────────▼───────┐  ┌────────▼──────┐  ┌─────────▼──────┐  ┌────────────▼─────┐
     │  integracoes   │  │   impressao   │  │ processo-core  │  │  processo-query  │
     │ implementa     │  │ observa       │  │ motor (único)  │  │ projeções,       │
     │ ports do core  │  │ ProcessoCriado│  │ segmento.*     │  │ somente leitura  │
     │ (cadastro)     │  │ → sistema PDF │  │ sem HTTP       │  │ não vê o core    │
     └────────┬───────┘  └───────┬───────┘  └──▲──────┬──────┘  └─────────┬────────┘
              └──────────────────┴─────────────┘      │                   │
                     dependem do core                 └─────────┬─────────┘
                                                      ┌─────────▼─────────┐
                                                      │     jpa-core      │  @Entity pai/filhas e Flyway
                                                      └───────────────────┘
```

| Módulo | Responsabilidade |
|---|---|
| `jpa-core` | Única "verdade" do schema: `ProcessoEntity` (pai, abstrata), `ProcessoImobiliarioEntity` (filha) e migrations |
| `processo-core` | Toda a lógica de negócio da escrita, sem HTTP: o `motor` (criar, processar e cancelar únicos, máquina de estados no padrão State, regras comuns, numeração, SPI `Segmento`, evento `ProcessoCriado`) e os segmentos (`segmento.imobiliario`, `segmento.veicular`: dados, regras, decisão do processamento e persistência da filha) |
| `processo-query` | Consultas somente leitura que devolvem projeções: listagem de todos os segmentos e detalhe de cada tipo |
| `integracoes` | Integrações de saída, um pacote por sistema externo: implementam ports do core e traduzem o modelo externo (anticorrupção). Exemplo: `cadastro`, consultado no ato da criação |
| `impressao` | Ponte para o sistema de PDF: pede o documento de abertura de todo processo criado, de qualquer segmento |
| `processo-app` | Todo o REST, o mapeamento de erros, a montagem do deploy único, os ITs e o ArchUnit entre módulos |

## API

| Método e caminho | Lado | O que faz |
|---|---|---|
| `POST /processos/imobiliarios`, `POST /processos/veiculares` | core | cria pelo motor único; responde `201 {"id", "numero"}` com `Location` para o detalhe |
| `POST /processos/{id}/processamento` | core | processa, para qualquer tipo; responde `200 {"situacao", "motivo"}` (`CONCLUIDO` ou `RECUSADO`) |
| `POST /processos/{id}/cancelamento` | core | cancela um processo `ABERTO`; responde `204` |
| `GET /processos/imobiliarios/{id}`, `GET /processos/veiculares/{id}` | query | detalhe com os campos do pai e da filha, incluindo situação e motivo da recusa |
| `GET /processos?situacao=ABERTO` | query | listagem de todos os segmentos, só com os campos comuns; filtro opcional |

Códigos de erro, todos com corpo `{"motivo": "..."}`:

| Código | Quando |
|---|---|
| `422` | regra violada ou transição inválida, por exemplo "Processo CONCLUIDO não pode ser cancelado" |
| `409` | outra requisição mudou o processo no meio |
| `404` | processo inexistente, ou situação inválida no filtro da listagem |
| `503` | sistema de cadastro fora do ar ou timeout |

Exemplo de corpo do `POST`:

```json
{
  "proponente": {"nome": "Ana Souza", "cpf": "52998224725"},
  "valor": 400000.00,
  "imovel": {"matricula": "12345", "tipo": "RESIDENCIAL", "valorAvaliacao": 500000.00, "uf": "SP"}
}
```

## Os fluxos de escrita

```
POST /processos/imobiliarios                                     (processo-app)
  → MotorDeProcessamento.criar(Solicitacao<DadosImobiliarios>)   (processo-core, o mesmo para todos os segmentos)
      1. escolhe o Segmento pela classe dos dados
      2. regras comuns (nome, CPF, valor > 0)
      3. regras do segmento (matrícula, UF, tipo, avaliação, limite de 80%/60%, matrícula sem processo aberto)
      3b. situação cadastral do CPF no sistema externo (port SituacaoCadastral → integracoes.cadastro)
      ── até aqui fora de transação; daqui em diante, transação curta ──
      4. número IMO-2026-000042 (prefixo do segmento + sequence única)
      5. segmento grava PROCESSO + PROCESSO_IMOBILIARIO
      6. publica ProcessoCriado
  → 201
                         depois do commit: impressao → POST /documentos no sistema de PDF

POST /processos/{id}/processamento                               (igual para todos os tipos)
  → MotorDeProcessamento.processar(id)
      1. lê o tipo (coluna SEGMENTO) e o segmento carrega o processo
      2. ABERTO → EM_PROCESSAMENTO (State) e compare-and-set     | transação curta; 409 se outro mudou antes
      3. segmento.avaliar(processo)                                | fora de transação
      4. EM_PROCESSAMENTO → CONCLUIDO | RECUSADO (State)           | transação curta
```

Falha no sistema de PDF não desfaz o processo: fica no log.

## Integrações com sistemas externos

| Tipo | Onde fica | Exemplo aqui |
|---|---|---|
| O negócio precisa da resposta no meio do fluxo | port no domínio do core, implementado em `integracoes.<sistema>` | `SituacaoCadastral` → `integracoes.cadastro` |
| Reação a um fato, sem resposta para o fluxo | módulo que observa o evento; o core nem sabe que existe | `impressao` |
| O sistema externo nos chama | entrada no `processo-app`, ou módulo próprio se for descartável | REST do app |

Como está o exemplo:

- `CadastroClient` é o REST Client do sistema externo (`GET /situacoes/{documento}`), com endereço em
  `quarkus.rest-client.cadastro.url`.
- `CadastroClientSimulado` é a simulação do perfil dev e responde sempre `REGULAR`, sem rede.
  `@IfBuildProfile("dev")` faz o bean existir só no build de dev, e `@RestClient @Alternative @Priority` o
  coloca no lugar do REST Client gerado. No jar de produção não há simulação.
- `RespostaCadastro` é o modelo externo, e não sai do pacote.
- `SituacaoCadastralAdapter` implementa o port do motor e converte a resposta. Ele injeta `@RestClient
  CadastroClient` e não sabe se recebeu o real ou o simulado.
- O motor só chama o sistema externo depois que todas as outras regras passaram.

| Onde | O que responde |
|---|---|
| `quarkus:dev` | `CadastroClientSimulado`, sem rede |
| jar de produção | REST Client, chamada HTTP para `CADASTRO_URL` |
| ITs (`@QuarkusIntegrationTest`, rodam o jar) | REST Client contra o `CadastroFalso`, um servidor HTTP do teste que responde `SUSPENSA` para um CPF |
| testes unitários | o adapter recebe um `CadastroClient` em lambda |

Sistema externo novo é um pacote novo em `integracoes`. Se ele tiver ciclo de vida próprio ou for descartável,
ganha um módulo próprio, como a `impressao`. Se o módulo de integração sair do deploy, o build do Quarkus falha
com `Unsatisfied dependency for type ...SituacaoCadastral`, em vez de falhar em produção.

## Adicionar um segmento

1. **No `processo-core`**: pacote `segmento.<nome>`, com um record de dados que implementa `DadosDoSegmento`, um
   bean que implementa `Segmento<D>` (incluindo `carregar` e a decisão `avaliar`) e o adapter de persistência.
2. **No `jpa-core`**: entidade filha de `ProcessoEntity` com `@DiscriminatorValue`, mais a migration da tabela.
3. **No `processo-query`**: a consulta de detalhe na filha. A listagem já inclui o segmento novo.
4. **No `processo-app`**: os endpoints de criação e detalhe do segmento.
5. **Nos testes**: um `Tipo` novo em `CicloDeVidaPorTipoIT.tipos()`.

O motor, a máquina de estados, os endpoints de ciclo de vida e a impressão não mudam. O commit que adicionou o
veicular é o exemplo.

## Remover a impressão

Apague a pasta `impressao`, a linha `<module>` e o `<dependency>` no POM pai, a dependência no `processo-app` e a
propriedade `quarkus.rest-client.sistema-pdf.url`. O `ArquiteturaModulosTest` garante que nenhum outro módulo
usa classes dela.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes unitários (sem Docker)
mvn -pl processo-core,integracoes,impressao -am test

# Build completo + testes de integração (o Dev Services sobe um Oracle Free no Docker)
mvn verify

# Modo dev: Oracle via Dev Services e o sistema de cadastro simulado (CadastroClientSimulado)
mvn install -DskipTests && mvn -pl processo-app quarkus:dev

# Jar empacotado contra o Oracle do docker-compose
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=processo QUARKUS_DATASOURCE_PASSWORD=processo \
SISTEMA_PDF_URL=http://localhost:8089 CADASTRO_URL=http://localhost:8090 \
java -jar processo-app/target/quarkus-app/quarkus-run.jar
```

## Sonar e cobertura

Mesmo arranjo validado na branch `sonar-multi-module`: `target/` padrão em cada módulo, o CI roda
`mvn clean verify` e depois o `sonar-maven-plugin` com `-DskipTests`.

- O POM pai define `sonar.java.source` e `sonar.coverage.jacoco.xmlReportPaths`, que aponta para o relatório
  agregado em `processo-app/target/site/jacoco-aggregate/jacoco.xml`.
- O `processo-app` define `sonar.junit.reportPaths` com `surefire-reports` (ArchUnit) e `failsafe-reports` (ITs).
- Os ITs ficam fora da cobertura, então o REST do `processo-app` e as consultas do `processo-query` aparecem com
  0% no Sonar, porque só os ITs os exercitam.
- Módulo novo precisa ser dependência direta do `processo-app` para entrar no relatório agregado.

## Documentação

- [`docs/guia.html`](docs/guia.html): guia completo da estrutura (módulos, fluxos, modelo de dados, integrações,
  regras de arquitetura, receitas e pontos em aberto). Abra no navegador.
- [`docs/design.md`](docs/design.md): decisões e armadilhas.
