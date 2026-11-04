## Mapa do projeto

<!-- Seção lida pelas skills layered-*. Troque os <marcadores> pelos nomes REAIS deste projeto e apague
     linhas que não se aplicam. Exemplo preenchido: copilot-instructions.md do repositório pagamento-boleto. -->

| Papel | Neste projeto |
|---|---|
| Pacote base | `<pacote.base>` (`src/main/java/<pacote/base>`) |
| Entrypoints | `entrypoint/api/v1` (REST), `<entrypoint/iib/v1 ou webhook/v1>` (integração de entrada) |
| Agregados | `<Agregado>` (`domain/entity/<Agregado>`) |
| Base de specs | `domain/specification/Specification`, `RegraNegocio`, `ValidadorRegras` |
| Catálogo de regras | `Regras<Agregado>` — por momento: `<SECAO>` (salvar seção), `<FINALIZACAO>` (completude), `<CONFORMIDADE>` (classifica fatos) |
| Candidatos das regras | `domain/specification/<agregado>/`: `<candidato da seção>`, `<candidato da finalização>` + `<Condicoes>.de(clock, ...)` |
| Agregado editado por partes | `<Agregado>`: `EM_EDICAO` → `Salvar<Secao><Agregado>UC` → `<Finalizar><Agregado>UC` |
| Máquina de estados | `domain/state`: `Estado<Agregado>`, estados concretos e enum `Status<Agregado>` |
| Repositórios (injetados nos UCs) | `adapter/database/<Agregado>Repository` (Panache, fala domínio) |
| Integrações externas (injetadas nos UCs) | `adapter/<sigla-do-sistema>/<Nome>` |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` |
| Builder de cenários de teste | `src/test/.../domain/Cenarios` |
| Teste de tabela de estados | `Estado<Agregado>Test` (mapa `PERMITIDAS`) |
| Comandos | `mvn test`, `mvn quarkus:dev` |
