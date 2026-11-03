## Mapa do projeto

<!-- Seção lida pelas skills layered-*. Mantenha os nomes em dia. Apague linhas que não se aplicam. -->

| Papel | Neste projeto |
|---|---|
| Pacote base | `br.com.empresa.sistema` (`src/main/java/br/com/empresa/sistema`) |
| Entrypoints | `entrypoint/api/v1` (REST), `entrypoint/iib/v1` (mensageria) |
| Agregados | `Contrato` (`domain/entity/Contrato`) |
| Base de specs | `domain/specification/Specification`, `RegraNegocio`, `ValidadorRegras` |
| Catálogo de regras | `RegrasContrato` — conjuntos `COMUNS`, `CRIACAO`, `ALTERACAO` |
| Candidatos das regras | `domain/specification/contrato/SolicitacaoContrato` (`paraCriacao`/`paraAlteracao`) |
| Máquina de estados | `domain/state`: `EstadoContrato`, estados concretos e enum `StatusContrato` |
| Repositórios (injetados nos UCs) | `adapter/database/ContratoRepository` (Panache, fala domínio) |
| Integrações externas (injetadas nos UCs) | `adapter/<sigla-do-sistema>/<Nome>` |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` |
| Builder de cenários de teste | `src/test/.../domain/Cenarios` |
| Teste de tabela de estados | `EstadoContratoTest` (mapa `PERMITIDAS`) |
| Comandos | `mvn test`, `mvn quarkus:dev` |
