## Mapa do projeto

<!-- Lido pela skill layered-crud. Troque os <marcadores> pelos nomes REAIS deste projeto e apague
     linhas que não se aplicam. Exemplo preenchido: copilot-instructions.md do repositório pagamento-boleto. -->

| Papel | Neste projeto |
|---|---|
| Pacote base | `<pacote.base>` (`src/main/java/<pacote/base>`) |
| Entrypoints | `entrypoint/api/v1` (REST), `<entrypoint/webhook/v1 ou iib/v1>` (entrada de sistema externo) |
| Agregados | `<Agregado>` (`domain/entity/<Agregado>`) |
| Catálogo de regras | `domain/specification/<agregado>/Regras<Agregado>`: `<SECAO>`, `<FINALIZACAO>` (cadeia), `<CONFORMIDADE>` |
| Variação por tipo (cadeia) | `<campo de tipo>`: `<TIPO_A>`, `<TIPO_B>` |
| Máquina de estados | `domain/state/Status<Agregado>` (enum com comportamento) |
| Casos de uso | `application/<agregado>/`: `<Criar…UC>`, `<Salvar<Secao>UC>`, `<invocador de comandos, se houver>` |
| Repositório (lock) | `domain/repository/<Agregado>Repository`: `obter`, `obterParaAlteracao`, `listar(<filtros>)` |
| Integrações externas | `adapter/<sistema>/<Classe>` (saída), `entrypoint/webhook/v1/<Resource>` (entrada) |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` |
| Testes | `src/test/.../domain/Cenarios`, `Status<Agregado>Test`, `<Agregado>ResourceTest` |
| Comandos | `mvn test`, `mvn quarkus:dev` |
