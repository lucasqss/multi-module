# Guia: adicionando módulos

Os módulos Maven deste projeto existem para **separar o design** e **limitar o acoplamento**, não para gerar
entregas separadas:

- **uma entrega:** o build produz um único jar executável, `target/biblioteca.jar`, com todos os módulos dentro;
- **um `target/`:** tudo que o build gera fica no `target/` da raiz (cada módulo compila em
  `target/<artifactId>/`); nenhum módulo tem `target/` próprio;
- **acoplamento verificado em dois níveis:** o Maven impede um módulo de usar o que não declarou como
  dependência, e o ArchUnit impede o que o Maven não alcança — entre módulos no `ArquiteturaModulosTest`
  (`biblioteca-app`) e dentro de um módulo em testes do próprio módulo (ex.: camadas do command).

Os exemplos abaixo usam um módulo hipotético `biblioteca-notificacao`; troque pelo nome real.

## 1. Escolha o tipo do módulo

| Tipo | Exemplo | `quarkus-maven-plugin` | `jandex-maven-plugin` | `application.properties` |
|---|---|---|---|---|
| **Biblioteca Java pura**: regra sem CDI, sem Quarkus | `string-generator` | não | não | não |
| **Biblioteca Quarkus**: tem `@ApplicationScoped`, `@Path`, `@Entity`, `@ConfigMapping`... | `jpa-core`, `biblioteca-command`, `biblioteca-query` | não | **sim** | não |
| **Aplicação**: gera o jar único | só o `biblioteca-app` | **sim** | não precisa | **sim** |

Todo módulo novo é uma **biblioteca**. Só existe um módulo de aplicação: um segundo geraria uma segunda entrega.

Na dúvida entre as duas bibliotecas, prefira a pura: é mais fácil de testar e de reaproveitar (o query chama os
métodos estáticos do `string-generator` sem CDI).

## 2. Passo a passo

### 2.1 Crie o diretório e o `pom.xml`

Use o mesmo nome no diretório e no `artifactId`. O `artifactId` também nomeia a pasta do módulo dentro do
`target/` (`target/biblioteca-notificacao/`).

```xml
<project ...>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>multi-module-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>biblioteca-notificacao</artifactId>

    <dependencies>
        <!-- Dependências do Quarkus SEM <version>: vêm do BOM importado no pai -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-arc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- Só se o módulo tiver beans CDI, recursos REST, entidades etc. -->
    <build>
        <plugins>
            <plugin>
                <groupId>io.smallrye</groupId>
                <artifactId>jandex-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Não repita `groupId`, `version`, `maven.compiler.release`, versões de plugins nem `<build><directory>`: tudo isso
é herdado.

### 2.2 Registre o módulo no POM pai

Dois lugares em `pom.xml` da raiz:

```xml
<modules>
    ...
    <module>biblioteca-notificacao</module>   <!-- nome do DIRETÓRIO -->
</modules>

<dependencyManagement>
    <dependencies>
        ...
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>biblioteca-notificacao</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

A ordem em `<modules>` não importa: o reactor calcula a ordem de build pelas dependências.

### 2.3 Ligue o módulo ao app

Declare a dependência em quem usa, **sem versão** — no `biblioteca-app/pom.xml` ou em outro módulo:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>biblioteca-notificacao</artifactId>
</dependency>
```

O módulo precisa chegar ao `biblioteca-app`, direta ou transitivamente (`app → command → notificacao`). É isso
que o coloca no jar único, nas regras do `ArquiteturaModulosTest` (que roda no app e enxerga o classpath dele) e
na cobertura agregada (que também roda no app, o último módulo do reactor).

### 2.4 Pacote raiz e registro no ArchUnit

- Um pacote raiz exclusivo por módulo: `com.example.biblioteca.notificacao` ([3.5](#35-um-pacote-raiz-por-módulo)).
- Registre-o no mapa `MODULOS` do `biblioteca-app/src/test/java/.../ArquiteturaModulosTest.java`. Esquecer não
  passa despercebido: a regra `todaClassePertenceAUmModulo` falha apontando a classe sem módulo.
- Escreva as regras de fronteira do módulo novo no mesmo teste (de quem ele pode e não pode depender), como
  `stringGeneratorEhJavaPuro` e `jpaCoreSoConheceOSchema`.
- Detalhes de implementação que precisam ser `public` (para outro pacote do próprio módulo) vão em
  `..notificacao.interno`: a regra `pacotesInternoSoSaoUsadosPeloProprioModulo` reprova outro módulo que os use.

### 2.5 Valide

```bash
mvn clean verify                                       # tudo, a partir da raiz (precisa de Docker)
mvn clean -pl biblioteca-notificacao -am test          # só o módulo novo e suas dependências
mvn clean -pl biblioteca-app -am test                  # + regras do ArchUnit entre módulos, sem Docker
```

Se o módulo tem beans, um teste que suba o Quarkus (os `*IT` do app) é o que prova que eles foram descobertos:
um teste unitário dentro do próprio módulo passa mesmo sem o índice Jandex.

### Checklist

- [ ] diretório = `artifactId`
- [ ] `<parent>` apontando para `multi-module-parent`
- [ ] entrada em `<modules>` **e** em `<dependencyManagement>` do pai
- [ ] o módulo chega ao `biblioteca-app` (direta ou transitivamente)
- [ ] dependências sem `<version>` (Quarkus pelo BOM, internos pelo pai)
- [ ] `jandex-maven-plugin` se houver beans/REST/entidades/config mapping
- [ ] **sem** `quarkus-maven-plugin`, **sem** `application.properties` e **sem** `<build><directory>` próprio
- [ ] pacote raiz exclusivo, registrado em `MODULOS`, com regras de fronteira no `ArquiteturaModulosTest`
- [ ] `mvn clean verify` verde na raiz, e nada de `biblioteca-notificacao/target/` criado
- [ ] testes do módulo em `target/surefire-reports/` e as classes em `target/site/jacoco-aggregate/`

## 3. Ao que atentar

### 3.1 Sem índice Jandex, os beans da biblioteca somem

O Quarkus descobre beans no build lendo um índice das classes. O módulo da aplicação é indexado
automaticamente; um jar de dependência, não. Sem o `jandex-maven-plugin` na biblioteca, um bean injetado em
outro módulo quebra o build do app com `UnsatisfiedResolutionException: Unsatisfied dependency for type ...`.
Pior é quando nada injeta a classe: um recurso `@Path` numa biblioteca sem índice é ignorado **sem erro
nenhum** e o endpoint responde 404; entidades `@Entity` seguem a mesma regra. (Os dois comportamentos foram
verificados num projeto mínimo, removendo o plugin.)

### 3.2 `quarkus-maven-plugin` só no módulo da aplicação

O plugin transforma o módulo num executável Quarkus. Numa biblioteca o build **não falha**, mas gera um segundo
pacote que ninguém usa e deixa o build mais lento. Por isso ele fica em `<pluginManagement>` no pai e só o
`biblioteca-app` o ativa.

### 3.3 `application.properties` só na aplicação

O `application.properties` de um jar de dependência **também é lido** pelo Quarkus, com o mesmo ordinal (250)
do arquivo do app; numa chave definida nos dois lugares, quem vence depende da ordem do classpath (verificado).
Regras:

- valores padrão de uma biblioteca vão em `@WithDefault` num `@ConfigMapping` ou, se precisar de arquivo, em
  `META-INF/microprofile-config.properties` (ordinal 100, perde para o app);
- `application.properties` só no `biblioteca-app`, que sobrescreve o que quiser;
- use um prefixo próprio por módulo (`biblioteca.notificacao.*`).

### 3.4 Versões: BOM e `dependencyManagement`

- Dependências gerenciadas pelo BOM do Quarkus (`io.quarkus:*`, JUnit, RestAssured, Jackson, Hibernate,
  `jakarta.data-api`...) **nunca** levam `<version>` nos módulos.
- Biblioteca de terceiros fora do BOM: versão numa `<property>` e entrada em `<dependencyManagement>` do pai;
  os módulos declaram sem versão (no jar único só cabe uma versão de cada lib).
- Para atualizar o Quarkus, troque só `quarkus.platform.version` no pai.

### 3.5 Um pacote raiz por módulo

Não use o mesmo pacote em dois módulos (*split package*): classes "package-private" passam a se enxergar
entre módulos — no jar único elas convivem no mesmo classpath — e o `ArquiteturaModulosTest` atribuiria a classe
ao módulo errado.

### 3.6 Direção das dependências e ciclos

O Maven rejeita ciclos entre módulos, e o ArchUnit rejeita ciclos entre **pacotes**, inclusive dentro de um
mesmo módulo (`pacotesSemCiclos`). Quando duas partes precisam uma da outra, extraia a parte comum ou inverta a
dependência com uma interface — é o que o command faz com os ports do `domain`, implementados em
`adapter.persistence`. A dependência aponta sempre para o mais estável: app → command/query → jpa-core e
string-generator, nunca o contrário.

### 3.7 Regras de arquitetura (ArchUnit)

| Onde | Regra | O que impede |
|---|---|---|
| `ArquiteturaModulosTest` (app) | `todaClassePertenceAUmModulo` | classe em pacote não registrado em `MODULOS` |
| | `pacotesSemCiclos` | ciclo entre quaisquer pacotes de `com.example` |
| | `commandEQueryNaoSeEnxergam` | um lado do CQRS usar o outro (no jar único eles convivem) |
| | `stringGeneratorEhJavaPuro` | `string-generator` depender de Jakarta, Quarkus, Hibernate ou de outro módulo |
| | `jpaCoreSoConheceOSchema` | `jpa-core` depender de command, query ou string-generator |
| | `restEJsonSoNasBordas` | REST/JSON fora de `command.adapter.rest` e do query |
| | `pacotesInternoSoSaoUsadosPeloProprioModulo` | um módulo usar `..interno..` de outro |
| `ArquiteturaTest` (command) | `dominioNaoConheceInfraestrutura` | `domain` depender de Jakarta, Quarkus, Hibernate, `jpa-core`, `application` ou `adapter` |

Ao que atentar:

- **JUnit 6:** o BOM do Quarkus 3.33 traz JUnit 6, e o engine `archunit-junit5` não executa sob ele (roda 0
  testes, sem erro). Use a API core: `regra.check(classes)` dentro de um `@Test` comum.
- **Classes de teste** ficam fora pelo filtro `/test-classes/` no `ClassFileImporter`. O
  `ImportOption.DO_NOT_INCLUDE_TESTS` procura `target/test-classes` e **não** reconhece o layout
  `target/<artifactId>/test-classes` deste build.
- **O ArchUnit lê bytecode**, não código-fonte: o tipo de uma variável local sem uso não gera dependência e não
  é pego. Chamadas, campos, parâmetros, retornos e herança são.
- Uma regra que não encontra classes falha com "failed to check any classes" (proposital). Se a regra só vai
  valer no futuro, use `.allowEmptyShould(true)`.

### 3.8 Testes entre módulos

- Classes de `src/test` **não** são visíveis em outro módulo. Para utilitários de teste compartilhados, crie um
  módulo `biblioteca-test-support` e dependa dele com `<scope>test</scope>` (ele não entra no jar; registre o
  pacote dele em `MODULOS` só se tiver classes em `src/main`).
- Teste regra com JUnit puro e fakes dos ports (como `CirculacaoServiceTest` com `EmMemoria`): rápido e sem
  Quarkus nem banco.
- `@QuarkusIntegrationTest` fica no `biblioteca-app` e roda na fase `verify` (failsafe) contra o
  `target/biblioteca.jar`, com Oracle do Dev Services (Docker) — `mvn test` não os executa.

### 3.9 Rotas REST de vários módulos no mesmo app

Use o caminho completo no `@Path` da classe em vez de `@Path("/")` + caminho no método. O JAX-RS escolhe
primeiro a classe raiz mais específica e não volta atrás: com `@Path("/leitores")` no command, um `@Path("/")` +
`leitores/{id}/painel` no query dá 404 (armadilha 5 de [`design.md`](design.md)).

### 3.10 Um `target/` só

```
target/
├─ biblioteca.jar                     a entrega: java -jar target/biblioteca.jar
├─ <artifactId>/                      saída de compilação de cada módulo (classes, jar intermediário, jacoco.exec)
├─ surefire-reports/  failsafe-reports/
└─ site/jacoco-aggregate/             cobertura (jacoco.xml para o Sonar, index.html para pessoas)
```

Como funciona, para não quebrar sem perceber:

- O POM pai define `target.raiz` = `${maven.multiModuleProjectDirectory}/target` e
  `<build><directory>${target.raiz}/${project.artifactId}</directory>`. A variável é a pasta que contém o
  `.mvn/`; **sem o `.mvn/`**, ela vira a pasta onde o `mvn` foi executado e, rodando de dentro de um módulo,
  o build volta a criar `target/` dentro dele.
- Nunca escreva `target/...` literal em configuração de plugin: use `${project.build.directory}` (pasta do
  módulo) ou `${target.raiz}` (raiz). É o que o `jpa-core` faz ao copiar o índice de entidades do Hibernate
  Processor. Plugin que assume o padrão cria `<módulo>/target/` de novo — foi o que o `@QuarkusIntegrationTest`
  fez com o `quarkus.log` até o failsafe receber `quarkus.log.file.path`.
- O `clean` da raiz apaga o `target/` inteiro e **não é herdado** (`<inherited>false</inherited>`): se fosse,
  cada módulo apagaria o que os anteriores construíram no mesmo `mvn clean verify`. Ao rodar só um módulo, use
  `mvn clean -pl <módulo> -am ...` (o `-am` inclui a raiz, que limpa tudo).
- Os jars intermediários (`target/jpa-core/jpa-core-1.0.0-SNAPSHOT.jar` etc.) continuam existindo: é assim que o
  Maven passa um módulo para o outro. A entrega é só o `target/biblioteca.jar`.
- `mvn -pl biblioteca-app verify` sem `-am` resolve os outros módulos do repositório local (`~/.m2`), que pode
  estar desatualizado ou nem existir. Use `-am`.

### 3.11 O jar único

Configurado no `biblioteca-app/src/main/resources/application.properties`:

```properties
quarkus.package.jar.type=uber-jar
quarkus.package.output-directory=..        # relativo a target/biblioteca-app/ → target/
quarkus.package.output-name=biblioteca
quarkus.package.jar.add-runner-suffix=false
```

O uber-jar tem cerca de 56 MB (o driver Oracle é a maior parte) e traz o `pom.properties` de cada dependência,
o que permite a ferramentas de SCA identificá-las. Para container, o formato padrão do Quarkus (`fast-jar`,
um diretório `quarkus-app/`) aproveita melhor o cache de camadas da imagem. Se trocar, ajuste também o caminho
do jar na cobertura agregada ([3.12](#312-relatórios-de-teste-e-cobertura-sonar)).

### 3.12 Relatórios de teste e cobertura (Sonar)

- Surefire e Failsafe de todos os módulos gravam em `target/surefire-reports/` e `target/failsafe-reports/`
  (`TEST-<pacote>.<Classe>.xml`; pacotes exclusivos por módulo evitam colisão de nomes).
- Um módulo **não** deve configurar `<reportsDirectory>` nem `<argLine>` próprios no Surefire/Failsafe: isso
  tira os relatórios da raiz ou desliga o agente do JaCoCo. Para argumentos de JVM nos testes, mantenha o
  `@{argLine}`: `<argLine>@{argLine} -Xmx1g</argLine>`.
- A cobertura soma os testes unitários (agente no Surefire de cada módulo) e os ITs (agente no processo do jar,
  via `quarkus.test.arg-line`, gravando `target/biblioteca-app/jacoco-it.exec`).
- O relatório é gerado no `biblioteca-app` (último do reactor) pela tarefa Ant do JaCoCo, que acha os módulos por
  convenção: `target/*/jacoco*.exec`, `target/*/classes` e `*/src/main/java`. Não há lista de módulos para
  manter. O `report-aggregate` do plugin Maven não serve aqui: ele só procura os `.exec` dentro da pasta de cada
  módulo.
- Os bytes das classes vêm do `target/biblioteca.jar`: o Quarkus transforma entidades, repositórios e recursos
  REST no build, e com os `.class` originais o JaCoCo descartaria a cobertura dos ITs dessas classes
  (armadilha 10 de [`design.md`](design.md)). Consequência: uma classe que o Quarkus transforma **e** que tem
  teste unitário perde a cobertura do teste unitário — hoje não há nenhuma (os unitários cobrem domínio,
  casos de uso e ABNT).
- O POM pai declara `sonar.junit.reportPaths` e `sonar.coverage.jacoco.xmlReportPaths` com esses caminhos.

### 3.13 Se o módulo novo tiver JPA / Jakarta Data

As armadilhas 1 a 6 e 9 de [`design.md`](design.md) valem para qualquer módulo novo com entidades ou
repositórios. As principais:

- entidades vão no `jpa-core` (única "verdade" do schema) e precisam do índice Jandex;
- o índice de entidades do Hibernate Processor (`${project.build.directory}/generated-sources/annotations/entity/index/**`)
  precisa ir dentro do jar do `jpa-core` — sem ele o HQL dos outros módulos **não é validado, sem aviso**;
- não mapeie coleções: HQL que percorre coleção também deixa de ser validado, sem aviso;
- o driver JDBC e a configuração do datasource ficam no `biblioteca-app`; não configure a URL do banco em
  `%prod` (desliga o Dev Services nos ITs).
