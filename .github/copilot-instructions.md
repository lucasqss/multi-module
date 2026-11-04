# Instruções do repositório — financiamento

**CRUD de referência** (Quarkus 3, Java 25, Maven, projeto único em `src/`): consistência com os padrões
existentes vale mais que a solução mais curta. Na dúvida, copie a forma de uma classe equivalente.
Código, nomes e mensagens em **português**; termos técnicos consagrados (Repository, Spec, UC) em inglês.

Domínio: **financiamento** de dois tipos (`IMOVEL`, `VEICULO`), com exigências SIMPLIFICADAS (é exemplo, não
sistema). O bem é avaliado por um sistema externo (**avaliação**); a garantia é registrada por outro (**cartório**
ou **órgão de trânsito**), que devolve o resultado por webhook. Um agregado e um fluxo para os dois tipos.

**Antes de criar ou alterar código, siga a skill `.github/skills/layered-crud/SKILL.md`.**

## Regras inegociáveis

1. Camadas `entrypoint → application → (domain, adapter)`, `adapter → domain`; domain só usa framework de
   persistência. **Sem ports and adapters**: o UC injeta classes concretas (repositório e clientes externos).
   **Menos classes sem custo de entendimento**: DTO/mapper só entre entrypoint e application; VO/record só com ganho real.
2. Domínio rico: a entidade JPA é a de domínio (sem setters, construtor sem argumentos `protected`, sem
   `PanacheEntity`); ordem **estado (409) → regras (422) → muda**.
3. Caso de uso = uma intenção, uma classe, um `executar`, uma transação; orquestra, não decide.
   Transições usam **Command** com o invocador único; UC não chama UC; nada de `*Service`/`*Utils`.
4. Regras = Specification no catálogo (regra simples = uma linha `de(...)`); variação por tipo = elo da `CadeiaValidacao`.
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
| Pacote base | `com.example.financiamento` (`src/main/java/com/example/financiamento`) |
| Entrypoints | `entrypoint/api/v1` (REST), `entrypoint/webhook/v1` (retorno do registro da garantia) |
| Agregados | `Financiamento` (`domain/entity/Financiamento`) |
| Catálogo de regras | `domain/specification/financiamento/RegrasFinanciamento`: `DOCUMENTOS`, `SUBMISSAO` (cadeia), `CONFORMIDADE_REGISTRO` |
| Variação por tipo (cadeia) | `TipoFinanciamento`: `IMOVEL`, `VEICULO` |
| Máquina de estados | `domain/state/StatusFinanciamento` (enum com comportamento) |
| Casos de uso | `application/financiamento/`: `CriarFinanciamentoUC`, `InformarGarantiaUC`, `SalvarDocumentosUC`, `ExecutarComandoFinanciamentoUC` (+ `comando/`) |
| Repositório (lock) | `domain/repository/FinanciamentoRepository`: `listar(tipo, status, ...)`, `obter`, `obterParaAlteracao` (também para o webhook: `IdProtocolo` carrega o financiamento) |
| Integrações externas | `adapter/avaliacao/AvaliadorDeBens`, `adapter/registro/RegistroDeGarantias` (saída); `entrypoint/webhook/v1/RegistroWebhookResource` (entrada) |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` (`financiamento.fuso-horario`) |
| Testes | `src/test/.../domain/Cenarios`, `StatusFinanciamentoTest`, `FinanciamentoRepositoryTest`, `FinanciamentoResourceTest`, `FinanciamentoResourceIT` (`@QuarkusIntegrationTest`) |
| Banco | Oracle em todos os profiles; dev/test via Dev Services (Docker); prod por `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` |
| Comandos | `mvn test`, `mvn verify` (integração), `mvn quarkus:dev` |
