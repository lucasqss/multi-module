# Modelo multi-module — Quarkus 3.33 · Java 25 · Maven

Esqueleto de uma aplicação Quarkus dividida em módulos Maven **para separar o design e limitar o
acoplamento**, não para gerar entregas separadas:

- o build gera **um único jar**, `target/modelo.jar`, com todos os módulos dentro;
- existe **um único `target/`**, na raiz: cada módulo compila em `target/<artifactId>/`;
- o acoplamento é verificado pelo **Maven** (um módulo só enxerga o que declarou como dependência) e pelo
  **ArchUnit** (sem ciclos entre pacotes, pacotes `interno` fechados, bibliotecas sem REST/JSON).

```
modelo-parent (pom.xml da raiz)   agrega os módulos e centraliza versões, plugins e o target/ único
├─ .mvn/                          marca a raiz do projeto (não remova: o target/ único depende dele)
├─ modelo-core/                   biblioteca: regra de negócio + beans CDI
│  └─ ...core.interno             detalhes de implementação, proibidos para outros módulos
└─ modelo-app/                    aplicação: REST, application.properties, regras ArchUnit, jar único
                                  └─ depende de modelo-core
```

| Módulo | Tipo | O que mostra |
|---|---|---|
| `modelo-core` | biblioteca | bean `@ApplicationScoped` descoberto via índice Jandex; `@ConfigMapping` com padrão em `@WithDefault`; pacote `interno`; teste JUnit puro, sem subir o Quarkus |
| `modelo-app` | aplicação | único módulo com `quarkus-maven-plugin` e `application.properties`; REST sobre o core; `ArquiteturaTest` (ArchUnit); `@QuarkusTest` e `@QuarkusIntegrationTest`; empacota o uber-jar e agrega a cobertura |

O POM pai importa o BOM do Quarkus, gerencia a versão dos módulos internos e roda o `maven-enforcer-plugin`,
que falha logo no início se o JDK for anterior ao 25 ou o Maven anterior ao 3.9.6.

## Como rodar

Pré-requisitos: JDK 25 e Maven 3.9.6+. Não precisa de Docker.

```bash
# Build completo: testes unitários, ArchUnit, @QuarkusTest, jar único, @QuarkusIntegrationTest e cobertura
mvn clean verify

# A entrega
java -jar target/modelo.jar

# Só um módulo (e o que ele precisa): -pl escolhe, -am inclui as dependências e a raiz
mvn clean -pl modelo-core -am test

# Modo dev a partir da raiz: não precisa de "mvn install" e recarrega também alterações no modelo-core
mvn quarkus:dev
```

```bash
curl localhost:8080/saudacoes/Ana     # {"nome":"Ana","mensagem":"Olá, Ana!"}
curl localhost:8080/saudacoes/%20     # 400 {"motivo":"nome é obrigatório"}
```

## O que fica no `target/`

```
target/
├─ modelo.jar                         a entrega (uber-jar com todos os módulos)
├─ modelo-core/  modelo-app/          saída de compilação de cada módulo
├─ surefire-reports/TEST-*.xml        testes unitários, ArchUnit e @QuarkusTest de todos os módulos
├─ failsafe-reports/TEST-*.xml        @QuarkusIntegrationTest
└─ site/jacoco-aggregate/jacoco.xml   cobertura de todos os módulos (também index.html)
```

O POM pai declara `sonar.junit.reportPaths` e `sonar.coverage.jacoco.xmlReportPaths` apontando para esses
caminhos (`mvn clean verify sonar:sonar`, com `sonar.host.url`/`sonar.token` configurados). Use sempre
`clean`: o da raiz apaga o `target/` inteiro, inclusive relatórios de execuções anteriores.

## Adicionando módulos

O passo a passo e as armadilhas (Jandex, `application.properties` em biblioteca, regras do ArchUnit, o
`target/` único, versões, pacotes, rotas, JPA...) estão em [`docs/adicionando-modulos.md`](docs/adicionando-modulos.md).

Um exemplo maior, com cinco módulos, CQRS, Jakarta Data e Oracle, está na branch `feat/biblioteca-cqrs`.
