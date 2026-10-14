# Guia: adicionando módulos

Este guia mostra como acrescentar um módulo ao modelo e o que costuma dar errado. Os exemplos usam um módulo
hipotético `modelo-pedidos`; troque pelo nome real.

## 1. Escolha o tipo do módulo

| Tipo | Quando usar | `quarkus-maven-plugin` | `jandex-maven-plugin` | `application.properties` |
|---|---|---|---|---|
| **Biblioteca Java pura** | regra sem CDI, sem Quarkus (cálculos, formatação, validações) | não | não | não |
| **Biblioteca Quarkus** (como o `modelo-core`) | tem `@ApplicationScoped`, `@Path`, `@Entity`, `@ConfigMapping`, observers... | não | **sim** | não |
| **Aplicação** (como o `modelo-app`) | gera um executável | **sim** | não precisa | **sim** |

Na dúvida entre as duas bibliotecas, prefira a pura: é mais fácil de testar e de reaproveitar. Ela pode ser
chamada diretamente por um bean de outro módulo.

## 2. Passo a passo

### 2.1 Crie o diretório e o `pom.xml`

Use o mesmo nome no diretório e no `artifactId`, com o prefixo do projeto (`modelo-`). Um jar chamado só
`core-1.0.0.jar` é ambíguo fora do projeto.

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

Não repita `groupId`, `version`, `maven.compiler.release` nem versões de plugins: tudo isso é herdado.

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

### 2.3 Inclua o módulo na cobertura

No `modelo-cobertura/pom.xml`, adicione o módulo novo em `<dependencies>` (sem versão). O relatório agregado
do JaCoCo só considera os módulos dos quais o `modelo-cobertura` depende: esquecer este passo não dá erro,
o módulo simplesmente some da cobertura no Sonar.

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>modelo-pedidos</artifactId>
</dependency>
```

### 2.4 Declare a dependência em quem usa

No `modelo-app/pom.xml` (ou em outra biblioteca), **sem versão**:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>modelo-pedidos</artifactId>
</dependency>
```

### 2.5 Crie o pacote raiz do módulo

Um pacote raiz exclusivo por módulo: `com.example.modelo.pedidos`. Veja o porquê em [3.5](#35-um-pacote-raiz-por-módulo).

### 2.6 Valide

```bash
mvn verify                             # tudo, a partir da raiz
mvn -pl modelo-pedidos -am test        # só o módulo novo e suas dependências
```

Se o módulo tem beans, o `@QuarkusTest` do app é o que prova que eles foram descobertos: um teste unitário
dentro do próprio módulo passa mesmo sem o índice Jandex.

### Checklist

- [ ] diretório = `artifactId`, com prefixo do projeto
- [ ] `<parent>` apontando para `modelo-parent`
- [ ] entrada em `<modules>` **e** em `<dependencyManagement>` do pai
- [ ] dependência no `modelo-cobertura/pom.xml`
- [ ] dependências sem `<version>` (Quarkus pelo BOM, internos pelo pai)
- [ ] `jandex-maven-plugin` se houver beans/REST/entidades/config mapping
- [ ] **sem** `quarkus-maven-plugin` e **sem** `application.properties` se for biblioteca
- [ ] pacote raiz exclusivo
- [ ] nenhuma dependência circular
- [ ] `mvn clean verify` verde na raiz, com os testes do módulo em `target/surefire-reports/` e as classes em
      `target/site/jacoco-aggregate/` da raiz

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
verificado). Entidades `@Entity` seguem a mesma regra. Alternativas ao plugin, piores por espalharem a configuração: um `META-INF/beans.xml` vazio na
biblioteca ou `quarkus.index-dependency.<nome>.group-id/artifact-id` no `application.properties` do app.

### 3.2 `quarkus-maven-plugin` só no módulo da aplicação

O plugin transforma o módulo num executável Quarkus. Numa biblioteca o build **não falha**, mas gera um
`target/quarkus-app/` que ninguém usa (verificado neste modelo), deixa o build mais lento e confunde quem
procura o executável. Por isso ele fica em `<pluginManagement>` no pai e só o `modelo-app` o ativa. Se precisar de **dois executáveis** (ex.: uma API e um worker), crie dois módulos de
aplicação, cada um com o plugin e o próprio `application.properties`, compartilhando as bibliotecas.

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
  os módulos declaram sem versão. Assim dois módulos nunca usam versões diferentes da mesma lib.
- Para atualizar o Quarkus, troque só `quarkus.platform.version` no pai.
- Para conferir o que o BOM gerencia: `mvn -pl modelo-app help:effective-pom` ou
  `mvn -pl modelo-app dependency:tree`.

### 3.5 Um pacote raiz por módulo

Não use o mesmo pacote em dois módulos (*split package*): classes "package-private" passam a se enxergar
entre jars diferentes, o que esconde acoplamento, e quebra se um dia o projeto usar JPMS
(`module-info.java`). Convenção: `com.example.modelo.<sufixo-do-módulo>`.

### 3.6 Direção das dependências e ciclos

O Maven rejeita ciclos (`The projects in the reactor contain a cyclic reference`). Quando dois módulos
precisam um do outro, extraia a parte comum para um terceiro módulo, ou inverta a dependência com uma
interface (a biblioteca define a interface, o app implementa). A dependência deve apontar sempre para o
módulo mais estável: app → bibliotecas → bibliotecas puras, nunca o contrário.

Uma biblioteca não deve conhecer detalhes de entrega: o `modelo-core` lança `IllegalArgumentException` e
quem a traduz para HTTP 400 é o `ErrosMapper` do `modelo-app`.

### 3.7 Testes entre módulos

- Classes de `src/test` **não** são visíveis em outro módulo. Se várias bibliotecas precisam dos mesmos
  utilitários de teste, crie um módulo `modelo-test-support` e dependa dele com `<scope>test</scope>`.
- Teste a regra da biblioteca com JUnit puro (instanciando as classes, como `SaudacaoServiceTest`): é rápido e
  não depende do Quarkus.
- `@QuarkusTest` e `@QuarkusIntegrationTest` ficam no módulo da aplicação. Os `*IT` rodam na fase `verify`
  (failsafe) contra o jar empacotado — `mvn test` não os executa.

### 3.8 Rotas REST de vários módulos no mesmo app

Se mais de um módulo expõe REST, use o caminho completo no `@Path` da classe (`@Path("/pedidos")`) em vez de
`@Path("/")` + caminho no método. O JAX-RS escolhe primeiro a classe raiz mais específica e não volta atrás:
`@Path("/leitores")` num módulo e `@Path("/")` + `leitores/{id}/painel` em outro resultam em 404.
(Armadilha encontrada no exemplo da branch `feat/biblioteca-cqrs`.)

### 3.9 Rodando: `-pl`, `-am` e modo dev

- `mvn -pl modelo-app verify` sozinho resolve o `modelo-core` do repositório local (`~/.m2`) — que pode estar
  desatualizado ou nem existir. Use `-am` para construir as dependências junto: `mvn -pl modelo-app -am verify`.
- `mvn quarkus:dev` na raiz sobe o app e recarrega alterações em **qualquer** módulo do reactor, sem
  `mvn install` (verificado alterando o `modelo-core` com o dev mode rodando).
- Depois de adicionar um módulo, reimporte o projeto Maven na IDE.

### 3.10 Relatórios de teste e cobertura na raiz (Sonar)

Como funciona, para não quebrar sem perceber:

- O POM pai define `relatorios.dir` = `${maven.multiModuleProjectDirectory}/target`. Essa variável é a pasta
  que contém o `.mvn/`; **sem o `.mvn/`**, ela vira a pasta onde o `mvn` foi executado e, rodando de dentro de
  um módulo, os relatórios voltam para o `target/` do módulo (verificado).
- Surefire e Failsafe de todos os módulos gravam na mesma pasta. Os arquivos se chamam
  `TEST-<pacote>.<Classe>.xml`: duas classes de teste com o mesmo nome completo em módulos diferentes se
  sobrescrevem. O pacote raiz exclusivo por módulo ([3.5](#35-um-pacote-raiz-por-módulo)) evita isso.
- Um módulo **não** deve configurar `<reportsDirectory>` nem `<argLine>` próprios no Surefire/Failsafe: isso
  tira os relatórios da raiz ou desliga o agente do JaCoCo. Se precisar de argumentos de JVM nos testes,
  mantenha o `@{argLine}`: `<argLine>@{argLine} -Xmx1g</argLine>`.
- O pai é construído antes de todos os módulos, por isso a cobertura é agregada no `modelo-cobertura`, que é
  construído por último. Módulo fora das dependências dele não aparece na cobertura ([2.3](#23-inclua-o-módulo-na-cobertura)).
- A cobertura vem dos testes unitários e dos `@QuarkusTest`. Os `@QuarkusIntegrationTest` rodam o jar em outro
  processo, sem o agente, e não contam para a cobertura — no modelo eles repetem os cenários do `@QuarkusTest`.
- Sempre `mvn clean ...`: o `clean` da raiz apaga o `target/` com os relatórios antigos. Sem ele, e
  principalmente com `-pl`, sobram relatórios de execuções anteriores.

### 3.11 Se o módulo novo tiver JPA / Jakarta Data

Pontos validados no exemplo da branch `feat/biblioteca-cqrs`:

- entidades em módulo-biblioteca precisam do índice Jandex (3.1);
- o driver JDBC (`quarkus-jdbc-*`) e a configuração do datasource ficam no módulo da aplicação;
- para o `hibernate-processor` validar HQL de `@Query` que cita entidades de **outro** módulo, o índice de
  entidades que ele gera (`target/generated-sources/annotations/entity/index/**`) precisa ser copiado para
  dentro do jar do módulo das entidades — sem isso a validação é pulada **sem aviso**;
- declare `@Column(name = ...)` em campos compostos (`dataPrevista` não vira `DATA_PREVISTA` sozinho);
- não configure a URL do banco em `%prod` se usar Dev Services nos `@QuarkusIntegrationTest`, que rodam no
  perfil `prod`.

## 4. Renomeando o modelo para um projeto real

1. `groupId` no pai e no `<parent>` de cada módulo, e nas entradas de `<dependencyManagement>`.
2. `artifactId` do pai e dos módulos, nomes dos diretórios e entradas em `<modules>`.
3. Pacotes Java (`com.example.modelo.*`) e o prefixo de configuração (`modelo.saudacao`).
4. `mvn clean verify` na raiz.
