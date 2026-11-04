# pagamento-boleto — CRUD de referência

Referência de **CRUD com padrões de projeto e organização em camadas** (Quarkus 3, Java 25, Maven,
projeto único em `src/`). O exemplo é o **pagamento de boletos**: o boleto é de um sistema externo
(o **emissor**); o pagamento é enviado a outro (o **banco**), que devolve o resultado por webhook.
As regras de negócio são hipotéticas.

```
 EMISSOR (externo)            Nosso sistema                  BANCO (externo)
 ┌──────────────┐  consulta  ┌──────────────────┐   envio    ┌──────────────┐
 │ dono do      │◀───────────│    Pagamento     │───────────▶│ liquidação   │
 │ boleto       │            │ (nosso agregado) │◀──webhook──│ (retorno)    │
 └──────────────┘            └──────────────────┘            └──────────────┘
```

## Camadas

```
entrypoint ──▶ application ──▶ domain          (ligadas por injeção de dependência de classes
                    │             ▲             concretas; sem ports and adapters)
                    └─▶ adapter ──┘
```

| Pacote (`com.example.pagamento`) | Papel |
|---|---|
| `domain` | `entity`, `valueobject`, `exception`, `specification`, `state` — só `java.*`, sem framework |
| `application/pagamento` | casos de uso (`*UC`), `comando/` (Command) e `dto/` (Input/Output) |
| `adapter` | `database/` (Panache + lock pessimista), `emissor/` e `banco/` (simulados) |
| `entrypoint` | `api/v1` (nossa API REST) e `webhook/v1` (entrada do banco) |
| `shared` | `Pagina`, `ClockProducer` |

## O agregado

Um agregado `Pagamento`, com a **modalidade** fixa na criação:

| Modalidade | Boletos | Regra própria |
|---|---|---|
| `INDIVIDUAL` | exatamente 1 | — |
| `PROCESSO` | 1 a 20 | todos do mesmo beneficiário |
| `LOTE` | 2 a 100 | total até R$ 1.000.000,00 |

Comuns a todas: tem boletos, conta de débito e data informadas, data não retroativa e em dia útil,
nenhum boleto vence antes da data de pagamento, sem boleto repetido.

```
EM_EDICAO ─submeter─▶ AGENDADO ─enviar─▶ EM_PROCESSAMENTO ─retorno liquidado conforme─▶ PAGO
   │                    │                    ├─ retorno recusado ─▶ RECUSADO ─reenviar─▶ EM_PROCESSAMENTO
   │                    │                    └─ liquidado ≠ total ─▶ EM_ANALISE
   └──── cancelar ──────┴──── (RECUSADO e EM_ANALISE também) ───▶ CANCELADO
```

O rascunho (`EM_EDICAO`) pode ficar incompleto e é editado **por seções** (dados gerais; boletos);
a completude só é exigida na submissão.

## Onde está cada padrão

| Padrão | Onde |
|---|---|
| Domínio rico | `domain/entity/Pagamento`: sem setters; ordem **estado (409) → regras (422) → muda** |
| Value objects | `domain/valueobject`: `Dinheiro`, `LinhaDigitavel`, `ContaDebito`, `BoletosDoPagamento`... |
| Specification | `domain/specification`: `Specification`, `RegraNegocio`, `ValidadorRegras`; specs em `pagamento/` |
| Chain of Responsibility | `CadeiaValidacao` + `RegrasPagamento.SUBMISSAO`: um elo comum e um elo por modalidade |
| State | `domain/state`: `EstadoPagamento` (sealed) e um estado por classe |
| Command | `application/pagamento/comando`: submeter, enviar, reenviar, cancelar; invocador `ExecutarComandoPagamentoUC` |
| Exceções por família | `domain/exception`: 400 / 404 / 409 / 422, mapeadas por `ApiExceptionHandler` |
| Comando × fato | retorno do banco não é recusado: é **classificado** (`EM_ANALISE` se diverge); idempotente via `IdTransacao` (aviso repetido ou antigo = 200) |
| Efeito após o commit | o envio ao banco só sai se a transação confirmou (`ExecutarComandoPagamentoUC.aposCommit`) |
| Lock pessimista | `PagamentoRepository.obterParaAlteracao` (`SELECT ... FOR UPDATE`); bloqueio → 409 `RECURSO_BLOQUEADO` |

## API

| Método e caminho | Caso de uso |
|---|---|
| `POST /api/v1/pagamentos` `{"modalidade":"LOTE"}` | `CriarPagamentoUC` |
| `GET /api/v1/pagamentos?modalidade=&status=&pagina=&tamanho=` | `ListarPagamentosUC` |
| `GET /api/v1/pagamentos/{id}` | `ConsultarPagamentoUC` |
| `PUT /api/v1/pagamentos/{id}` (conta e data) | `AlterarDadosPagamentoUC` |
| `PUT /api/v1/pagamentos/{id}/boletos` (linhas digitáveis) | `SalvarBoletosDoPagamentoUC` (consulta o emissor) |
| `DELETE /api/v1/pagamentos/{id}` | `ExcluirPagamentoUC` (só rascunho) |
| `POST /api/v1/pagamentos/{id}/submissao` · `/envio` · `/reenvio` · `/cancelamento` | `ExecutarComandoPagamentoUC` + comando |
| `POST /webhooks/v1/banco/retornos` (header `X-Webhook-Token`) | `ReceberRetornoBancoUC` |

Erros sempre no formato `{"codigo": "...", "mensagem": "...", "campos": [...]}`.

O emissor é simulado: a linha digitável (47 dígitos) segue um layout fake
`9` + emissor(3) + id(25) + vencimento `yyyyMMdd` + valor em centavos(10); começar com `0` = boleto indisponível.

## Rodar

```bash
mvn quarkus:dev   # http://localhost:8081
mvn test          # domínio (JUnit puro) + repositório/transação + ponta a ponta (@QuarkusTest, H2)
```

## Copilot e reuso em outros projetos

O padrão está numa skill única e genérica, [`.github/skills/layered-crud`](.github/skills/layered-crud/SKILL.md):
regras de camadas, entidade rica, caso de uso e Command, Specification + Chain, State, exceções,
integração externa, lock, testes e checklist de revisão — com este repositório só como ilustração.
A pasta traz também `templates/` (base genérica para iniciar um projeto), `mapa-do-projeto.md` e
`verificar-camadas.sh`. Para usar em outro projeto:

```bash
cp -r .github/skills/layered-crud <outro-projeto>/.github/skills/
# no outro projeto: peça ao Copilot "inicie o projeto no padrão da skill layered-crud"
bash .github/skills/layered-crud/verificar-camadas.sh
```
