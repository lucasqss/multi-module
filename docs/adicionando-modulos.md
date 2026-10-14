# Guia: adicionando módulos

Neste modelo os módulos Maven existem para **separar o design** e **limitar o acoplamento**, não para gerar
entregas separadas:

- **uma entrega:** o build produz um único jar executável, `target/modelo.jar`, com todos os módulos dentro;
- **um `target/`:** tudo que o build gera fica no `target/` da raiz (cada módulo compila em
  `target/<artifactId>/`); nenhum módulo tem `target/` próprio;
- **acoplamento verificado em dois níveis:** o Maven impede um módulo de usar o que não declarou como
  dependência, e o ArchUnit (`ArquiteturaTest`, no `modelo-app`) impede o que o Maven não alcança: ciclos entre
  pacotes, uso de pacotes `interno` de outro módulo e bibliotecas conhecendo REST/JSON.

Os exemplos abaixo usam um módulo hipotético `modelo-pedidos`; troque pelo nome real.

## 1. Escolha o tipo do módulo

| Tipo | Quando usar | `quarkus-maven-plugin` | `jandex-maven-plugin` | `application.properties` |
|---|---|---|---|---|
| **Biblioteca Java pura** | regra sem CDI, sem Quarkus (cálculos, formatação, validações) | não | não | não |
| **Biblioteca Quarkus** (como o `modelo-core`) | tem `@ApplicationScoped`, `@Path`, `@Entity`, `@ConfigMapping`, observers... | não | **sim** | não |
| **Aplicação** — só o `modelo-app` | gera o jar único | **sim** | não precisa | **sim** |

Todo módulo novo é uma **biblioteca**. Só existe um módulo de aplicação: um segundo geraria uma segunda entrega,
o que contraria o objetivo do modelo.

Na dúvida entre as duas bibliotecas, prefira a pura: é mais fácil de testar e de reaproveitar. Ela pode ser
chamada diretamente por um bean de outro módulo.

## 2. Passo a passo

### 2.1 Crie o diretório e o `pom.xml`

Use o mesmo nome no diretório e no `artifactId`, com o prefixo do projeto (`modelo-`). O `artifactId` também
nomeia a pasta do módulo dentro do `target/` (`target/modelo-pedidos/`).

```xml
<project ...>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>modelo-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>modelo-pedidos</artifactId>

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
    <module>modelo-core</module>
    <module>modelo-pedidos</module>   <!-- nome do DIRETÓRIO -->
    <module>modelo-app</module>
</modules>

<dependencyManagement>
    <dependencies>
        ...
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>modelo-pedidos</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

A ordem em `<modules>` não importa: o reactor calcula a ordem de build pelas dependências.

### 2.3 Ligue o módulo ao app

Declare a dependência em quem usa, **sem versão** — no `modelo-app/pom.xml` ou em outra biblioteca:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>modelo-pedidos</artifactId>
</dependency>
```

O módulo precisa chegar ao `modelo-app`, direta ou transitivamente (`app → pedidos` ou `app → core → pedidos`).
É isso que o coloca:

- no jar único (o Quarkus empacota as dependências do app);
- nas regras do ArchUnit (o teste roda no app e enxerga o classpath dele);
- na cobertura agregada (que também roda no app, o último módulo do reactor).

### 2.4 Crie o pacote raiz e separe a API do que é interno

- Um pacote raiz exclusivo por módulo: `com.example.modelo.pedidos`. Além de evitar *split packages*
  ([3.5](#35-um-pacote-raiz-por-módulo)), é **por ele que o ArchUnit identifica o módulo**: uma classe fora dessa
  convenção escapa das regras.
- O que outros módulos podem usar fica no pacote raiz (ou em subpacotes comuns). Detalhes de implementação que
  precisam ser `public` (para serem usados em outro pacote do próprio módulo) vão em
  `com.example.modelo.pedidos.interno`: o ArchUnit reprova qualquer outro módulo que os use. Exemplo:
  `com.example.modelo.core.interno.Nomes`.

### 2.5 Valide

```bash
mvn clean verify                             # tudo, a partir da raiz
mvn clean -pl modelo-pedidos -am test        # só o módulo novo e suas dependências
```

Se o módulo tem beans, o `@QuarkusTest` do app é o que prova que eles foram descobertos: um teste unitário
dentro do próprio módulo passa mesmo sem o índice Jandex.

### Checklist

- [ ] diretório = `artifactId`, com prefixo do projeto
- [ ] `<parent>` apontando para `modelo-parent`
- [ ] entrada em `<modules>` **e** em `<dependencyManagement>` do pai
- [ ] o módulo chega ao `modelo-app` (direta ou transitivamente)
- [ ] dependências sem `<version>` (Quarkus pelo BOM, internos pelo pai)
- [ ] `jandex-maven-plugin` se houver beans/REST/entidades/config mapping
- [ ] **sem** `quarkus-maven-plugin`, **sem** `application.properties` e **sem** `<build><directory>` próprio
- [ ] pacote raiz `com.example.modelo.<módulo>`; detalhes públicos em `.interno`
- [ ] `mvn clean verify` verde na raiz, e nada de `modelo-pedidos/target/` criado
- [ ] testes do módulo em `target/surefire-reports/` e as classes em `target/site/jacoco-aggregate/`

## 3. Ao que atentar

### 3.1 Sem índice Jandex, os beans da biblioteca somem

O Quarkus descobre beans no build lendo um índice das classes. O módulo da aplicação é indexado
automaticamente; um jar de dependência, não. Sem o `jandex-maven-plugin` na biblioteca, o build do app falha
com:

```
UnsatisfiedResolutionException: Unsatisfied dependency for type com.example.modelo.core.SaudacaoService
```

(Verificado neste modelo removendo o plugin do `modelo-core`.) Pior é quando nada injeta a classe: um recurso
`@Path` numa biblioteca sem índice é ignorado **sem erro nenhum** e o endpoint responde 404 (também
verificado). Entidades `@Entity` seguem a mesma regra. Alternativas ao plugin, piores por espalharem a
configuração: um `META-INF/beans.xml` vazio na biblioteca ou `quarkus.index-dependency.<nome>.group-id/artifact-id`
no `application.properties` do app.

### 3.2 `quarkus-maven-plugin` só no módulo da aplicação

O plugin transforma o módulo num executável Quarkus. Numa biblioteca o build **não falha**, mas gera um segundo
pacote que ninguém usa (verificado neste modelo) e deixa o build mais lento. Por isso ele fica em
`<pluginManagement>` no pai e só o `modelo-app` o ativa.

### 3.3 `application.properties` só na aplicação

O `application.properties` de um jar de dependência **também é lido** pelo Quarkus, com o mesmo ordinal (250)
do arquivo do app. Testado neste modelo:

- só o core com o arquivo → o valor do core é usado;
- core e app com o arquivo → o do app venceu, mas porque veio antes no classpath, não por regra.

Ou seja, uma chave definida nos dois lugares tem resultado frágil. Regras:

- valores padrão de uma biblioteca vão em `@WithDefault` no `@ConfigMapping` (como em `SaudacaoConfig`) ou,
  se precisar de arquivo, em `META-INF/microprofile-config.properties` (ordinal 100, perde para o app);
- `application.properties` só no módulo da aplicação, que sobrescreve o que quiser;
- use um prefixo próprio por módulo (`modelo.pedidos.*`) para não colidir com outro módulo.

### 3.4 Versões: BOM e `dependencyManagement`

- Dependências gerenciadas pelo BOM do Quarkus (`io.quarkus:*`, JUnit, RestAssured, Jackson, Hibernate...)
  **nunca** levam `<version>` nos módulos. Fixar uma versão manualmente desalinha a plataforma.
- Biblioteca de terceiros fora do BOM: versão numa `<property>` e entrada em `<dependencyManagement>` do pai;
  os módulos declaram sem versão. Assim dois módulos nunca usam versões diferentes da mesma lib (no jar único
  só cabe uma).
- Para atualizar o Quarkus, troque só `quarkus.platform.version` no pai.
- Para conferir o que o BOM gerencia: `mvn -pl modelo-app help:effective-pom` ou
  `mvn -pl modelo-app dependency:tree`.

### 3.5 Um pacote raiz por módulo

Não use o mesmo pacote em dois módulos (*split package*): classes "package-private" passam a se enxergar
entre módulos — no jar único elas convivem no mesmo classpath —, o que esconde acoplamento, e as regras do
ArchUnit atribuiriam a classe ao módulo errado. Convenção: `com.example.modelo.<sufixo-do-módulo>`.

### 3.6 Direção das dependências e ciclos

O Maven rejeita ciclos entre módulos (`The projects in the reactor contain a cyclic reference`), e o ArchUnit
rejeita ciclos entre **pacotes**, inclusive dentro de um mesmo módulo. Quando duas partes precisam uma da outra,
extraia a parte comum para um terceiro módulo ou pacote, ou inverta a dependência com uma interface (a
biblioteca define a interface, o app implementa). A dependência deve apontar sempre para o mais estável:
app → bibliotecas → bibliotecas puras, nunca o contrário.

Uma biblioteca não deve conhecer detalhes de entrega: o `modelo-core` lança `IllegalArgumentException` e
quem a traduz para HTTP 400 é o `ErrosMapper` do `modelo-app` (regra `soOAppConheceRestEJson`).

### 3.7 Regras de arquitetura (ArchUnit)

Ficam em `modelo-app/src/test/java/.../ArquiteturaTest.java`, porque o app é o único módulo com todos os outros
no classpath. As regras atuais valem para qualquer módulo que siga a convenção de pacote, sem editar o teste:

| Regra | O que impede |
|---|---|
| `pacotesSemCiclos` | ciclo entre quaisquer pacotes de `com.example.modelo` (entre módulos ou dentro de um) |
| `pacotesInternoSoSaoUsadosPeloProprioModulo` | um módulo usar `..interno..` de outro |
| `soOAppConheceRestEJson` | bibliotecas dependerem de JAX-RS, RESTEasy, Quarkus REST ou Jackson |

Ao que atentar:

- **JUnit 6:** o BOM do Quarkus 3.33 traz JUnit 6, e o engine `archunit-junit5` não executa sob ele (roda 0
  testes, sem erro). Use a API core: `regra.check(classes)` dentro de um `@Test` comum, como no modelo.
- **Classes de teste** ficam fora da análise pelo filtro `/test-classes/` no `ClassFileImporter`. O
  `ImportOption.DO_NOT_INCLUDE_TESTS` do ArchUnit procura `target/test-classes` e **não** reconhece o layout
  `target/<artifactId>/test-classes` deste modelo.
- **O ArchUnit lê bytecode**, não código-fonte: o tipo de uma variável local que não é usada não gera
  dependência e não é pego (verificado). Chamadas, campos, parâmetros, retornos e herança são.
- Regras específicas de um módulo (ex.: camadas `domain`/`application`/`adapter` dentro de `modelo-pedidos`)
  podem ficar no próprio módulo, num teste com `archunit` em `<scope>test</scope>`, importando só
  `com.example.modelo.pedidos`.
- Uma regra nova que não encontra classes falha com "failed to check any classes" — é proposital
  (`archRule.failOnEmptyShould`). Se a regra só vai valer no futuro, use `.allowEmptyShould(true)`.

### 3.8 Testes entre módulos

- Classes de `src/test` **não** são visíveis em outro módulo. Se várias bibliotecas precisam dos mesmos
  utilitários de teste, crie um módulo `modelo-test-support` e dependa dele com `<scope>test</scope>` (ele não
  entra no jar).
- Teste a regra da biblioteca com JUnit puro (instanciando as classes, como `SaudacaoServiceTest`): é rápido e
  não depende do Quarkus.
- `@QuarkusTest` e `@QuarkusIntegrationTest` ficam no módulo da aplicação. Os `*IT` rodam na fase `verify`
  (failsafe) contra o `target/modelo.jar` — `mvn test` não os executa.

### 3.9 Rotas REST de vários módulos no mesmo app

Se mais de um módulo expõe REST, use o caminho completo no `@Path` da classe (`@Path("/pedidos")`) em vez de
`@Path("/")` + caminho no método. O JAX-RS escolhe primeiro a classe raiz mais específica e não volta atrás:
`@Path("/leitores")` num módulo e `@Path("/")` + `leitores/{id}/painel` em outro resultam em 404.
(Armadilha encontrada no exemplo da branch `feat/biblioteca-cqrs`.)

### 3.10 Um `target/` só

```
target/
├─ modelo.jar                         a entrega: java -jar target/modelo.jar
├─ modelo-core/  modelo-app/          saída de compilação de cada módulo (classes, jar intermediário, jacoco.exec)
├─ surefire-reports/  failsafe-reports/
└─ site/jacoco-aggregate/             cobertura (jacoco.xml para o Sonar, index.html para pessoas)
```

Como funciona, para não quebrar sem perceber:

- O POM pai define `target.raiz` = `${maven.multiModuleProjectDirectory}/target` e
  `<build><directory>${target.raiz}/${project.artifactId}</directory>`. A variável é a pasta que contém o
  `.mvn/`; **sem o `.mvn/`**, ela vira a pasta onde o `mvn` foi executado e, rodando de dentro de um módulo,
  o build volta a criar `target/` dentro dele (verificado).
- Nunca escreva `target/...` literal em configuração de plugin: use `${project.build.directory}` (pasta do
  módulo) ou `${target.raiz}` (raiz). Plugin que assume o padrão cria `<módulo>/target/` de novo — foi o que o
  `@QuarkusIntegrationTest` fez com o `quarkus.log` até o failsafe receber `quarkus.log.file.path`.
- O `clean` da raiz apaga o `target/` inteiro e **não é herdado** (`<inherited>false</inherited>`): se fosse,
  cada módulo apagaria o que os anteriores construíram no mesmo `mvn clean verify`. Ao rodar só um módulo, use
  `mvn clean -pl <módulo> -am ...` para incluir a raiz e limpar tudo.
- Os jars intermediários (`target/modelo-core/modelo-core-1.0.0-SNAPSHOT.jar` etc.) continuam existindo: é
  assim que o Maven passa um módulo para o outro. A entrega é só o `target/modelo.jar`.
- IDEs (IntelliJ, VS Code/Eclipse) leem o `<build><directory>` do pom; reimporte o projeto Maven após mudar.
- `mvn quarkus:dev` na raiz funciona com esse layout e recarrega alterações em **qualquer** módulo, sem
  `mvn install` (verificado alterando o `modelo-core` com o dev mode rodando).
- `mvn -pl modelo-app verify` sem `-am` resolve o `modelo-core` do repositório local (`~/.m2`), que pode estar
  desatualizado ou nem existir. Use `-am`.

### 3.11 O jar único

Configurado no `modelo-app/src/main/resources/application.properties`:

```properties
quarkus.package.jar.type=uber-jar
quarkus.package.output-directory=..        # relativo a target/modelo-app/ → target/
quarkus.package.output-name=modelo
quarkus.package.jar.add-runner-suffix=false
```

O uber-jar tem cerca de 17 MB e inclui todos os módulos e dependências. Para container, o formato padrão do
Quarkus (`fast-jar`, um diretório `quarkus-app/`) aproveita melhor o cache de camadas da imagem, porque as
dependências mudam pouco; com o uber-jar cada deploy envia o jar inteiro. Para trocar, remova as quatro linhas
acima e copie `target/modelo-app/quarkus-app/` para a imagem.

### 3.12 Relatórios de teste e cobertura (Sonar)

- Surefire e Failsafe de todos os módulos gravam em `target/surefire-reports/` e `target/failsafe-reports/`.
  Os arquivos se chamam `TEST-<pacote>.<Classe>.xml`: duas classes de teste com o mesmo nome completo em
  módulos diferentes se sobrescrevem — o pacote raiz exclusivo ([3.5](#35-um-pacote-raiz-por-módulo)) evita isso.
- Um módulo **não** deve configurar `<reportsDirectory>` nem `<argLine>` próprios no Surefire/Failsafe: isso
  tira os relatórios da raiz ou desliga o agente do JaCoCo. Se precisar de argumentos de JVM nos testes,
  mantenha o `@{argLine}`: `<argLine>@{argLine} -Xmx1g</argLine>`.
- A cobertura agregada é gerada no `modelo-app` (último do reactor) pela tarefa Ant do JaCoCo, que encontra os
  módulos por convenção: `target/*/jacoco.exec`, `target/*/classes` e `*/src/main/java`. Não há lista de
  módulos para manter. O `report-aggregate` do plugin Maven não serve aqui: ele só procura os `.exec` dentro da
  pasta de cada módulo.
- A cobertura vem dos testes unitários e dos `@QuarkusTest`. Os `@QuarkusIntegrationTest` rodam o jar em outro
  processo, sem o agente, e não contam para a cobertura — no modelo eles repetem os cenários do `@QuarkusTest`.
- O POM pai declara `sonar.junit.reportPaths` e `sonar.coverage.jacoco.xmlReportPaths` com esses caminhos.

### 3.13 Se o módulo novo tiver JPA / Jakarta Data

Pontos validados no exemplo da branch `feat/biblioteca-cqrs`:

- entidades em módulo-biblioteca precisam do índice Jandex ([3.1](#31-sem-índice-jandex-os-beans-da-biblioteca-somem));
- o driver JDBC (`quarkus-jdbc-*`) e a configuração do datasource ficam no módulo da aplicação;
- para o `hibernate-processor` validar HQL de `@Query` que cita entidades de **outro** módulo, o índice de
  entidades que ele gera (`${project.build.directory}/generated-sources/annotations/entity/index/**`) precisa
  ser copiado para dentro do jar do módulo das entidades — sem isso a validação é pulada **sem aviso**;
- declare `@Column(name = ...)` em campos compostos (`dataPrevista` não vira `DATA_PREVISTA` sozinho);
- não configure a URL do banco em `%prod` se usar Dev Services nos `@QuarkusIntegrationTest`, que rodam no
  perfil `prod`.

## 4. Renomeando o modelo para um projeto real

1. `groupId` no pai e no `<parent>` de cada módulo, e nas entradas de `<dependencyManagement>`.
2. `artifactId` do pai e dos módulos, nomes dos diretórios e entradas em `<modules>`.
3. Pacotes Java (`com.example.modelo.*`), a constante `RAIZ` do `ArquiteturaTest` e o prefixo de configuração
   (`modelo.saudacao`).
4. O nome do jar (`quarkus.package.output-name`).
5. `mvn clean verify` na raiz.
