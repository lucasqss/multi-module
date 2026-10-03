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

## Decisões e armadilhas encontradas

O desenho completo e as armadilhas encontradas no caminho (validação do HQL que é pulada sem aviso, colisão de
rotas no deploy único etc.) estão em [`docs/design.md`](docs/design.md).
