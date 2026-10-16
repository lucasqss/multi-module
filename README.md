# Biblioteca — CQRS em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo de uma biblioteca (livros, exemplares, leitores e empréstimos) para mostrar:

- **CQRS com independência de design** (não de deploy): o lado de escrita tem domínio rico; o lado de leitura,
  só projeções. Um módulo não enxerga o outro.
- **Entidade de domínio ≠ entidade JPA**: o schema é mapeado uma única vez (`jpa-core`), o domínio é Java puro.
- **Jakarta Data** com HQL **validado em tempo de compilação** pelo Hibernate Processor.
- Um gerador de strings com regra real: referências e citações **ABNT** (NBR 6023 / NBR 10520).

> A versão anterior, mais simples (`crud` + `string-generator`), está na tag `v1-simples`.

## Módulos

```
            ┌──────────────────────┐
            │    biblioteca-app    │  sem classes Java: junta tudo num deploy
            │  (+ ITs caixa preta) │  e abriga os @QuarkusIntegrationTest
            └──────────┬───────────┘
          ┌────────────┴────────────┐
┌─────────▼──────────┐   ┌──────────▼─────────┐
│ biblioteca-command │   │  biblioteca-query  │──────┐
│ domain · application│   │ @Repository (leitura)│     │
│ adapter (JPA, REST) │   │ REST                │     │
└─────────┬──────────┘   └──────────┬─────────┘     │
          └────────────┬────────────┘      ┌────────▼─────────┐
              ┌────────▼────────┐          │ string-generator │
              │    jpa-core     │          │  Java puro (ABNT)│
              │ @Entity + Flyway│          └──────────────────┘
              └─────────────────┘
```

| Módulo | Responsabilidade |
|---|---|
| `jpa-core` | Única "verdade" do schema: entidades JPA (sem regra) e migrations Flyway |
| `string-generator` | Referência e citação ABNT a partir de records próprios — não conhece JPA nem domínio |
| `biblioteca-command` | Domínio (agregados, Specifications, ports), casos de uso, adapters Jakarta Data e REST de escrita |
| `biblioteca-query` | Consultas Jakarta Data somente-leitura e REST de leitura |
| `biblioteca-app` | Montagem do deploy único + testes de integração |

`biblioteca-query` não depende de `biblioteca-command`: o Maven impede o lado de leitura de importar o domínio.
Dentro do command, um teste ArchUnit impede o pacote `domain` de depender de JPA/Quarkus/adapters.

## API

| Lado | Método e caminho | O que faz |
|---|---|---|
| command | `POST /livros` | cadastra livro (autores em ordem) |
| command | `POST /livros/{id}/exemplares` | registra exemplar (tombo) |
| command | `DELETE /exemplares/{id}` | baixa do exemplar |
| command | `POST /leitores` | cadastra leitor |
| command | `POST /emprestimos` | empresta (`leitorId`, `exemplarId`) |
| command | `POST /emprestimos/{id}/renovacao` | renova (máx. 2, nunca atrasado) |
| command | `POST /emprestimos/{id}/devolucao` | devolve; atraso suspende o leitor 1 dia por dia |
| query | `GET /acervo?titulo=&autor=` | livros com referência ABNT e exemplares disponíveis |
| query | `GET /leitores/{id}/painel` | empréstimos abertos com citação `(ASSIS, 1899)` |
| query | `GET /emprestimos/atrasados` | relatório de atrasos |

Regras violadas respondem `422 {"motivo": "..."}`; recursos inexistentes, `404`.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes unitários (sem Docker)
mvn -pl string-generator,biblioteca-command -am test

# Build completo + testes de integração (o Dev Services sobe um Oracle Free no Docker)
mvn verify

# Modo dev (também com Oracle via Dev Services)
mvn install -DskipTests && mvn -pl biblioteca-app quarkus:dev

# Jar empacotado contra o Oracle do docker-compose
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=biblioteca QUARKUS_DATASOURCE_PASSWORD=biblioteca \
java -jar biblioteca-app/target/quarkus-app/quarkus-run.jar
```

## Sonar e cobertura

Cada módulo usa o `target/` padrão do Maven. O fluxo esperado é o de CI, em dois passos, sem nada no comando
além do que já se usa num projeto de módulo único:

```bash
mvn clean verify                       # 1. build, testes e relatórios (o report-aggregate roda aqui)
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar \
    -Dsonar.sources=pom.xml,src/main -DskipTests -Dsonar.host.url=... -Dsonar.login=...   # 2. análise
```

O scanner do Maven entende o multi-module sozinho: aplica `sonar.sources` relativo a cada módulo e lê
`<módulo>/target/classes` e `<módulo>/target/surefire-reports`. O que está nos POMs:

| Propriedade | Onde | Por quê |
|---|---|---|
| `sonar.coverage.jacoco.xmlReportPaths` | POM pai | todos os módulos leem o relatório agregado, `biblioteca-app/target/site/jacoco-aggregate/jacoco.xml`; cada um importa só os próprios arquivos |
| `sonar.java.source` | POM pai | o scanner 3.6 não lê `maven.compiler.release`; sem isso os `record` dão `Parse error at line 1 column 1` |
| `sonar.junit.reportPaths` | `biblioteca-app` | os `@QuarkusIntegrationTest` ficam em `failsafe-reports`, que o Sonar não lê por padrão |

- O agente do JaCoCo roda nos testes unitários de todos os módulos (`<módulo>/target/jacoco.exec`) e o
  `report-aggregate` do `biblioteca-app` junta tudo num relatório só, contando o teste de um módulo também para
  os outros.
- Os `@QuarkusIntegrationTest` contam como testes no Sonar, mas ficam **fora** da cobertura: rodam contra o jar
  em outro processo, sem agente.
- O `report-aggregate` só considera as dependências **diretas** do `biblioteca-app`: por isso ele declara também
  `jpa-core` e `string-generator`. Módulo novo precisa entrar nessa lista para aparecer na cobertura.
- O passo 2 não pode ter `clean`: ele lê os relatórios gerados no passo 1.

Validado com SonarQube Community 26.10 e o `sonar-maven-plugin` 3.6.0.1398: 56 testes (21 + 21 + 14 ITs) e
cobertura por módulo igual à do JaCoCo, sem erros na análise.

## Decisões e armadilhas encontradas

O desenho completo e as armadilhas encontradas no caminho (validação do HQL que é pulada sem aviso, colisão de
rotas no deploy único etc.) estão em [`docs/design.md`](docs/design.md).
