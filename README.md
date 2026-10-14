# Modelo multi-module — Quarkus 3.33 · Java 25 · Maven

Esqueleto mínimo de uma aplicação Quarkus dividida em módulos Maven, para servir de ponto de partida.
Tem só dois módulos, um de cada tipo:

```
modelo-parent (pom.xml da raiz)   agrega os módulos e centraliza versões e plugins
├─ .mvn/                          marca a raiz do projeto (não remova: veja "Relatórios para o Sonar")
├─ modelo-core/                   módulo-biblioteca: regra de negócio + beans CDI (jar comum)
└─ modelo-app/                    módulo da aplicação: REST, application.properties, executável Quarkus
                                  └─ depende de modelo-core; também agrega a cobertura de todos os módulos
```

| Módulo | Tipo | O que mostra |
|---|---|---|
| `modelo-core` | biblioteca | bean `@ApplicationScoped` descoberto via índice Jandex; `@ConfigMapping` com padrão em `@WithDefault`; teste JUnit puro, sem subir o Quarkus |
| `modelo-app` | aplicação | único módulo com `quarkus-maven-plugin` e `application.properties`; REST sobre o core; `@QuarkusTest` e `@QuarkusIntegrationTest`; relatório JaCoCo agregado |

O POM pai importa o BOM do Quarkus, gerencia a versão dos módulos internos e roda o `maven-enforcer-plugin`,
que falha logo no início se o JDK for anterior ao 25 ou o Maven anterior ao 3.9.6.

## Como rodar

Pré-requisitos: JDK 25 e Maven 3.9.6+. Não precisa de Docker.

```bash
# Build completo: testes unitários, @QuarkusTest e @QuarkusIntegrationTest contra o jar
mvn verify

# Só um módulo (e o que ele precisa): -pl escolhe, -am inclui as dependências do reactor
mvn -pl modelo-core test
mvn -pl modelo-app -am verify

# Modo dev a partir da raiz: não precisa de "mvn install" e recarrega também alterações no modelo-core
mvn quarkus:dev

# Jar empacotado
java -jar modelo-app/target/quarkus-app/quarkus-run.jar
```

```bash
curl localhost:8080/saudacoes/Ana     # {"nome":"Ana","mensagem":"Olá, Ana!"}
curl localhost:8080/saudacoes/%20     # 400 {"motivo":"nome é obrigatório"}
```

## Relatórios para o Sonar

Os relatórios de **todos** os módulos vão para o `target/` da raiz do repositório:

```
target/
├─ surefire-reports/TEST-*.xml            testes unitários e @QuarkusTest de todos os módulos
├─ failsafe-reports/TEST-*.xml            @QuarkusIntegrationTest
└─ site/jacoco-aggregate/jacoco.xml       cobertura agregada (também index.html para ver no navegador)
```

```bash
mvn clean verify                          # gera tudo acima
mvn clean verify sonar:sonar              # idem + análise (com sonar.host.url / sonar.token configurados)
```

O POM pai já declara `sonar.junit.reportPaths` e `sonar.coverage.jacoco.xmlReportPaths` apontando para esses
caminhos. A raiz é descoberta pelo diretório `.mvn/`, então o destino é o mesmo rodando o `mvn` da raiz ou de
dentro de um módulo. Use sempre `clean`: sem ele, relatórios de execuções anteriores continuam na pasta.

## Adicionando módulos

O passo a passo e as armadilhas (Jandex, `application.properties` em biblioteca, versões, pacotes, rotas,
JPA...) estão em [`docs/adicionando-modulos.md`](docs/adicionando-modulos.md).

Um exemplo maior, com cinco módulos, CQRS, Jakarta Data e Oracle, está na branch `feat/biblioteca-cqrs`.
