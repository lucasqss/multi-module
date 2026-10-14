# .mvn/

Marca a raiz do projeto Maven: a partir dele o Maven define `maven.multiModuleProjectDirectory`, usado no
`pom.xml` para gravar os relatórios de teste e cobertura de todos os módulos em `target/` da raiz, mesmo
quando o `mvn` é executado de dentro de um módulo. **Não remova este diretório.**

Aqui também podem ficar `maven.config` (argumentos padrão do `mvn`) e `jvm.config` (opções da JVM do Maven).
