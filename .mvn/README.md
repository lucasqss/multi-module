# .mvn/

Marca a raiz do projeto Maven: a partir dele o Maven define `maven.multiModuleProjectDirectory`, usado no
`pom.xml` para que o build tenha um único `target/`, na raiz (compilação de cada módulo, relatórios de teste e
cobertura e o jar final), mesmo quando o `mvn` é executado de dentro de um módulo. **Não remova este diretório.**

Aqui também podem ficar `maven.config` (argumentos padrão do `mvn`) e `jvm.config` (opções da JVM do Maven).
