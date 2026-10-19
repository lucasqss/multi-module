# Processo — motor único por segmento em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo só do **criar processo imobiliário**, para mostrar:

- **Um motor de processamento único** para todos os segmentos. O motor não conhece nenhum segmento: cada um se
  pluga implementando o SPI `Segmento`.
- **Processo imobiliário como filha do processo**: herança JPA `JOINED` (tabela `PROCESSO` + tabela
  `PROCESSO_IMOBILIARIO`).
- **Um módulo de impressão descartável**: a ponte para o sistema de geração de PDF. Ninguém depende dele, então
  removê-lo é apagar o módulo.

## Módulos

```
                    ┌──────────────────────────┐
                    │       processo-app       │  sem classes Java: junta tudo num deploy,
                    │ (+ ITs e ArchUnit)       │  ITs contra o jar e regras entre módulos
                    └─────┬──────────────┬─────┘
                          │              │
           ┌──────────────▼───────┐  ┌───▼──────────────────┐
           │ segmento-imobiliario │  │      impressao       │  descartável: observa
           │ dados, regras, REST, │  │ ProcessoCriado →     │  ProcessoCriado; ninguém
           │ grava a tabela filha │  │ sistema de PDF       │  depende dele
           └──────────────┬───────┘  └───┬──────────────────┘
                          │              │
                    ┌─────▼──────────────▼─────┐
                    │      processo-motor      │  motor ÚNICO: regras comuns, numeração,
                    │ SPI Segmento · evento    │  fluxo do criar; não conhece segmentos
                    └────────────┬─────────────┘
                    ┌────────────▼─────────────┐
                    │         jpa-core         │  @Entity (pai + filhas) e Flyway
                    └──────────────────────────┘
```

| Módulo | Responsabilidade |
|---|---|
| `jpa-core` | Única "verdade" do schema: `ProcessoEntity` (pai, abstrata), `ProcessoImobiliarioEntity` (filha) e migrations |
| `processo-motor` | `MotorDeProcessamento` (o fluxo do criar), regras comuns, numeração, SPI `Segmento` e o evento `ProcessoCriado` |
| `segmento-imobiliario` | `DadosImobiliarios`, regras do imóvel, `SegmentoImobiliario` (o plug no motor), gravação da filha e `POST /processos/imobiliarios` |
| `impressao` | Ponte para o sistema de PDF: pede o documento de abertura de todo processo criado, de qualquer segmento |
| `processo-app` | Montagem do deploy único, testes de integração e ArchUnit entre módulos |

## O fluxo do criar

```
POST /processos/imobiliarios
  → MotorDeProcessamento.criar(Solicitacao<DadosImobiliarios>)      ← o mesmo para todos os segmentos
      1. escolhe o Segmento pela classe dos dados
      2. regras comuns (nome, CPF, valor > 0)
      3. regras do segmento (matrícula, UF, tipo, avaliação, limite de 80%/60%, matrícula sem processo aberto)
      4. número IMO-2026-000042 (prefixo do segmento + sequence única)
      5. segmento grava PROCESSO + PROCESSO_IMOBILIARIO
      6. publica ProcessoCriado
  → 201 {"id": "...", "numero": "IMO-2026-000042"}
                                   depois do commit: impressao → POST /documentos no sistema de PDF
```

Regras violadas respondem `422 {"motivo": "..."}`. Falha no sistema de PDF não desfaz o processo: fica no log.

Exemplo de corpo:

```json
{
  "proponente": {"nome": "Ana Souza", "cpf": "52998224725"},
  "valor": 400000.00,
  "imovel": {"matricula": "12345", "tipo": "RESIDENCIAL", "valorAvaliacao": 500000.00, "uf": "SP"}
}
```

## Adicionar um segmento

1. Módulo `segmento-<nome>` dependendo de `processo-motor`.
2. Record de dados implementando `DadosDoSegmento` e um bean implementando `Segmento<D>` (código, prefixo,
   regras, gravação).
3. Entidade filha de `ProcessoEntity` no `jpa-core` com `@DiscriminatorValue`, mais a migration da tabela.
4. Endpoint REST do segmento chamando `MotorDeProcessamento.criar`.
5. Dependência no `processo-app` e o pacote em `MODULOS` e `SEGMENTOS` no `ArquiteturaModulosTest`.

O motor não muda, e a impressão passa a atender o segmento novo sozinha (modelo
`abertura-processo-<código>`).

## Remover a impressão

Apague a pasta `impressao`, a linha `<module>` e o `<dependency>` no POM pai, a dependência no `processo-app` e a
propriedade `quarkus.rest-client.sistema-pdf.url`. O `ArquiteturaModulosTest` garante que nenhum outro módulo
usa classes dela, então nada mais quebra.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes unitários (sem Docker)
mvn -pl processo-motor,segmento-imobiliario,impressao -am test

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
`mvn clean verify` e depois o `sonar-maven-plugin` com `-DskipTests`. O POM pai define
`sonar.coverage.jacoco.xmlReportPaths`, que aponta para o relatório agregado em
`processo-app/target/site/jacoco-aggregate/jacoco.xml`, e `sonar.java.source`. O `processo-app` define
`sonar.junit.reportPaths` com `surefire-reports` (ArchUnit) e `failsafe-reports` (ITs). Módulo novo precisa ser
dependência direta do `processo-app` para entrar na cobertura agregada.

## Decisões e armadilhas

Estão em [`docs/design.md`](docs/design.md).
