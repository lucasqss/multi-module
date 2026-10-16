# Biblioteca — CQRS em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo de uma biblioteca (livros, exemplares, leitores e empréstimos) para mostrar:

- **CQRS com independência de design** (não de deploy): o lado de escrita tem domínio rico; o lado de leitura,
  só projeções. Um módulo não enxerga o outro.
- **Entidade de domínio ≠ entidade JPA**: o schema é mapeado uma única vez (`jpa-core`), o domínio é Java puro.
- **Jakarta Data** com HQL **validado em tempo de compilação** pelo Hibernate Processor.
- Um gerador de strings com regra real: referências e citações **ABNT** (NBR 6023 / NBR 10520).

Os módulos existem para **separar o design e limitar o acoplamento**, não para gerar entregas separadas:

- o build gera **um único jar**, `target/biblioteca.jar`, com todos os módulos dentro;
- existe **um único `target/`**, na raiz: cada módulo compila em `target/<artifactId>/`, e relatórios de teste,
  cobertura e o jar ficam direto em `target/` (é onde Sonar, Xray e Checkmarx procuram);
- o acoplamento é verificado pelo **Maven** (um módulo só enxerga o que declarou como dependência) e pelo
  **ArchUnit** (regras entre módulos no `biblioteca-app`, camadas do domínio no `biblioteca-command`).

> A versão mais simples (`crud` + `string-generator`) está na branch `main`.

## Módulos

```
            ┌──────────────────────┐
            │    biblioteca-app    │  sem classes Java: junta tudo no jar único e abriga
            │ (ITs + ArchUnit +    │  os testes de API (*IT), as regras entre módulos
            │  cobertura agregada) │  e a cobertura agregada
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
| `biblioteca-app` | Montagem do jar único, testes de integração, regras de arquitetura entre módulos e cobertura agregada |

`biblioteca-query` não depende de `biblioteca-command`: o Maven impede o lado de leitura de importar o domínio.
Como no jar único as classes convivem no mesmo classpath, o `ArquiteturaModulosTest` (no `biblioteca-app`)
também verifica isso, além de: nenhum ciclo entre pacotes, `string-generator` em Java puro, `jpa-core` sem
conhecer os lados, REST/JSON só nas bordas e toda classe pertencendo a um módulo registrado. Dentro do command,
o `ArquiteturaTest` impede o pacote `domain` de depender de JPA/Quarkus/adapters.

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

Pré-requisitos: **JDK 25**, Maven 3.9.6+ e Docker. Com outro JDK o build para logo no início, de propósito
(`maven-enforcer-plugin`: `Detected JDK ... is version 21 ... not in the allowed range [25,)`).

```bash
# Testes unitários e de arquitetura (sem Docker)
mvn clean -pl biblioteca-app -am test

# Build completo: unitários, ArchUnit, jar único, testes de integração e cobertura
# (o Dev Services sobe um Oracle Free no Docker para os *IT)
mvn clean verify

# Modo dev (também com Oracle via Dev Services); recarrega alterações em qualquer módulo
mvn quarkus:dev

# O jar contra o Oracle do docker-compose
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=biblioteca QUARKUS_DATASOURCE_PASSWORD=biblioteca \
java -jar target/biblioteca.jar
```

## O que fica no `target/`

```
target/
├─ biblioteca.jar                     a entrega (uber-jar com todos os módulos)
├─ jpa-core/  string-generator/  biblioteca-command/  biblioteca-query/  biblioteca-app/
│                                     saída de compilação de cada módulo
├─ surefire-reports/TEST-*.xml        testes unitários e de arquitetura de todos os módulos
├─ failsafe-reports/TEST-*.xml        testes de API (*IT, @QuarkusTest com Oracle do Dev Services)
├─ jacoco/jacoco.exec                 dados de cobertura de todos os testes
└─ site/jacoco-aggregate/jacoco.xml   cobertura de todos os módulos, unitários + *IT (também index.html)
```

O POM pai declara `sonar.junit.reportPaths` e `sonar.coverage.jacoco.xmlReportPaths` apontando para esses
caminhos (`mvn clean verify sonar:sonar`, com `sonar.host.url`/`sonar.token` configurados). Use sempre
`clean`: o da raiz apaga o `target/` inteiro, inclusive relatórios e dados de cobertura de execuções anteriores
(que, sem ele, se acumulariam no `jacoco.exec`).

A cobertura usa só plugins Maven padrão e a extensão `quarkus-jacoco` (sem antrun); os detalhes estão em
[`docs/adicionando-modulos.md`](docs/adicionando-modulos.md#312-relatórios-de-teste-e-cobertura-sonar).

## Mais detalhes

- [`docs/design.md`](docs/design.md): decisões de design da biblioteca e as armadilhas encontradas (validação do
  HQL que é pulada sem aviso, colisão de rotas no deploy único etc.).
- [`docs/adicionando-modulos.md`](docs/adicionando-modulos.md): como adicionar um módulo e ao que atentar
  (Jandex, `application.properties`, regras do ArchUnit, `target/` único, jar único, cobertura).
