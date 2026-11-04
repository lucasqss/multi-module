# Instruções do repositório — pagamento-boleto

**CRUD de referência** (Quarkus 3, Java 25, Maven, projeto único em `src/`): consistência com os padrões
existentes vale mais que a solução mais curta. Na dúvida, copie a forma de uma classe equivalente.
Código, nomes e mensagens em **português**; termos técnicos consagrados (Repository, Spec, UC) em inglês.

Domínio: **pagamento de boletos** em três modalidades (`INDIVIDUAL`, `PROCESSO`, `LOTE`). O boleto é de um
sistema externo (o **emissor**); o pagamento é feito por outro (o **banco**), que devolve o resultado por webhook.

**Antes de criar ou alterar código, siga a skill `.github/skills/layered-crud/SKILL.md`.**

## Regras inegociáveis

1. Camadas `entrypoint → application → (domain, adapter)`, `adapter → domain`; domain só usa framework de
   persistência. **Sem ports and adapters**: o UC injeta classes concretas (repositório e clientes externos).
   **Menos classes sem custo de entendimento**: DTO/mapper só entre entrypoint e application; VO/record só com ganho real.
2. Domínio rico: a entidade JPA é a de domínio (sem setters, construtor sem argumentos `protected`, sem
   `PanacheEntity`); ordem **estado (409) → regras (422) → muda**.
3. Caso de uso = uma intenção, uma classe, um `executar`, uma transação; orquestra, não decide.
   Transições usam **Command** com o invocador único; UC não chama UC; nada de `*Service`/`*Utils`.
4. Regras = Specification no catálogo (regra simples = uma linha `de(...)`); variação por modalidade = elo da `CadeiaValidacao`.
5. Status = State pattern em enum com comportamento; nunca `if`/`switch` em status.
6. Exceção específica por situação, nas famílias 400/404/409/422; um handler HTTP por família.
7. Fato externo é classificado (não recusado), idempotente, e entra por `entrypoint/webhook/v1`.
8. Repositório com métodos de intenção que já tratam filtros; alterar = `obterParaAlteracao` (lock pessimista); ler = `obter`.
9. Estrutura enxuta: domain só com `entity`, `repository`, `valueobject`, `exception`, `specification`, `state`;
   `adapter/` só sistemas externos; `shared/` sem subpastas.

Verifique com `bash .github/skills/layered-crud/verificar-camadas.sh` e `mvn test`.

## Mapa do projeto

| Papel | Neste projeto |
|---|---|
| Pacote base | `com.example.pagamento` (`src/main/java/com/example/pagamento`) |
| Entrypoints | `entrypoint/api/v1` (REST), `entrypoint/webhook/v1` (retorno do banco) |
| Agregados | `Pagamento` (`domain/entity/Pagamento`) |
| Catálogo de regras | `domain/specification/pagamento/RegrasPagamento`: `BOLETOS`, `SUBMISSAO` (cadeia), `CONFORMIDADE_RETORNO` |
| Variação por tipo (cadeia) | `Modalidade`: `INDIVIDUAL`, `PROCESSO`, `LOTE` |
| Máquina de estados | `domain/state/StatusPagamento` (enum com comportamento) |
| Casos de uso | `application/pagamento/`: `CriarPagamentoUC`, `SalvarBoletosDoPagamentoUC`, `ExecutarComandoPagamentoUC` (+ `comando/`) |
| Repositório (lock) | `domain/repository/PagamentoRepository`: `listar(modalidade, status, ...)`, `obter`, `obterParaAlteracao` (também para o webhook: `IdTransacao` carrega o pagamento) |
| Integrações externas | `adapter/emissor/EmissorBoletos`, `adapter/banco/BancoPagamentos` (saída); `entrypoint/webhook/v1/BancoWebhookResource` (entrada) |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` (`pagamento.fuso-horario`) |
| Testes | `src/test/.../domain/Cenarios`, `StatusPagamentoTest`, `PagamentoRepositoryTest`, `PagamentoResourceTest`, `PagamentoResourceIT` (`@QuarkusIntegrationTest`) |
| Banco | Oracle em todos os profiles; dev/test via Dev Services (Docker); prod por `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` |
| Comandos | `mvn test`, `mvn verify` (integração), `mvn quarkus:dev` |
