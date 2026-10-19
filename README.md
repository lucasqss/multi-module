# Processo — motor único por segmento em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo do **criar e consultar processo imobiliário**, para mostrar:

- **Um motor de processamento único** para todos os segmentos. Motor e segmentos ficam no `processo-core`, e uma
  regra do ArchUnit impede o motor de conhecer qualquer segmento.
- **Processo imobiliário como filha do processo**: herança JPA `JOINED` (tabela `PROCESSO` + tabela
  `PROCESSO_IMOBILIARIO`).
- **CQRS**: o `processo-core` (escrita) e o `processo-query` (leitura) não se enxergam. Os dois só se encontram
  no REST do `processo-app`.
- **Um módulo de impressão descartável**: a ponte para o sistema de geração de PDF. Ninguém depende dele, então
  removê-lo é apagar o módulo.

## Módulos

```
                      ┌────────────────────────────┐
                      │        processo-app        │  todo o REST (criação e consulta),
                      │  REST · ITs · ArchUnit     │  montagem do deploy, ITs e regras entre módulos
                      └──┬──────────┬───────────┬──┘
                         │          │           │
     ┌───────────────────▼──┐  ┌────▼───────┐  ┌▼─────────────────┐
     │    processo-core     │◄─┤ impressao  │  │  processo-query  │  leitura: projeções,
     │ motor (único)        │  │ observa    │  │  Jakarta Data    │  não enxerga o core
     │ segmento.imobiliario │  │ Processo-  │  │  somente leitura │
     │ sem HTTP             │  │ Criado→PDF │  └────────┬─────────┘
     └──────────┬───────────┘  └────────────┘           │
                └─────────────────┬─────────────────────┘
                       ┌──────────▼──────────┐
                       │      jpa-core       │  @Entity pai/filhas e Flyway
                       └─────────────────────┘
```

| Módulo | Responsabilidade |
|---|---|
| `jpa-core` | Única "verdade" do schema: `ProcessoEntity` (pai, abstrata), `ProcessoImobiliarioEntity` (filha) e migrations |
| `processo-core` | Toda a lógica de negócio da escrita, sem HTTP: o `motor` (fluxo único do criar, regras comuns, numeração, SPI `Segmento`, evento `ProcessoCriado`) e os segmentos (`segmento.imobiliario`: dados, regras e gravação da filha) |
| `processo-query` | Consultas somente leitura que devolvem projeções: listagem de todos os segmentos e detalhe do imobiliário |
| `impressao` | Ponte para o sistema de PDF: pede o documento de abertura de todo processo criado, de qualquer segmento |
| `processo-app` | Todo o REST, o mapeamento de erros, a montagem do deploy único, os ITs e o ArchUnit entre módulos |

## API

| Método e caminho | Lado | O que faz |
|---|---|---|
| `POST /processos/imobiliarios` | core | cria pelo motor único; responde `201 {"id", "numero"}` com `Location` para o detalhe |
| `GET /processos/imobiliarios/{id}` | query | detalhe com os campos do pai e da filha |
| `GET /processos?situacao=ABERTO` | query | listagem de todos os segmentos, só com os campos comuns; filtro opcional |

Regras violadas respondem `422 {"motivo": "..."}`. Processo inexistente e situação inválida no filtro respondem
`404`.

Exemplo de corpo do `POST`:

```json
{
  "proponente": {"nome": "Ana Souza", "cpf": "52998224725"},
  "valor": 400000.00,
  "imovel": {"matricula": "12345", "tipo": "RESIDENCIAL", "valorAvaliacao": 500000.00, "uf": "SP"}
}
```

## O fluxo do criar

```
POST /processos/imobiliarios                                     (processo-app)
  → MotorDeProcessamento.criar(Solicitacao<DadosImobiliarios>)   (processo-core, o mesmo para todos os segmentos)
      1. escolhe o Segmento pela classe dos dados
      2. regras comuns (nome, CPF, valor > 0)
      3. regras do segmento (matrícula, UF, tipo, avaliação, limite de 80%/60%, matrícula sem processo aberto)
      4. número IMO-2026-000042 (prefixo do segmento + sequence única)
      5. segmento grava PROCESSO + PROCESSO_IMOBILIARIO
      6. publica ProcessoCriado
  → 201
                         depois do commit: impressao → POST /documentos no sistema de PDF
```

Falha no sistema de PDF não desfaz o processo: fica no log.

## Adicionar um segmento

1. **No `processo-core`**: pacote `segmento.<nome>`, com um record de dados que implementa `DadosDoSegmento`, um
   bean que implementa `Segmento<D>` e o adapter de gravação.
2. **No `jpa-core`**: entidade filha de `ProcessoEntity` com `@DiscriminatorValue`, mais a migration da tabela.
3. **No `processo-query`**: a consulta de detalhe na filha. A listagem já inclui o segmento novo.
4. **No `processo-app`**: os endpoints do segmento.

O motor não muda, e a impressão passa a atender o segmento novo sozinha (modelo `abertura-processo-<código>`).

## Remover a impressão

Apague a pasta `impressao`, a linha `<module>` e o `<dependency>` no POM pai, a dependência no `processo-app` e a
propriedade `quarkus.rest-client.sistema-pdf.url`. O `ArquiteturaModulosTest` garante que nenhum outro módulo
usa classes dela.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes unitários (sem Docker)
mvn -pl processo-core,impressao -am test

# Build completo + testes de integração (o Dev Services sobe um Oracle Free no Docker)
mvn verify

# Jar empacotado contra o Oracle do docker-compose
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=processo QUARKUS_DATASOURCE_PASSWORD=processo \
SISTEMA_PDF_URL=http://localhost:8089 \
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

## Decisões e armadilhas

Estão em [`docs/design.md`](docs/design.md).
