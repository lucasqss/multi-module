# consorcio-pagamento — CRUD de referência

CRUD de **processo de pagamento de consórcio** organizado em camadas, com:

- **domínio rico** (sem setters, sem anotações de framework);
- **Specification pattern** para regras de negócio (as regras são fake/hipotéticas);
- **State pattern** para a máquina de estados do processo;
- **use cases finos**, um por ação, que só orquestram.

## Camadas

```
entrypoint ──▶ application ──▶ domain ◀── adapter
 (HTTP)        (use cases)    (regras)    (JPA, sistemas externos)
```

| Pacote | Papel | Pode depender de |
|---|---|---|
| `entrypoint.api.v1` | Contrato HTTP, validação **sintática** (`@NotNull`), tradução de exceções → status | application |
| `application.processopagamento` | Um `*UC` por ação: busca fatos, chama o domínio, persiste, devolve DTO. `@Transactional` aqui | domain |
| `domain.entity` | `ProcessoPagamento` (aggregate root) | — |
| `domain.specification` | Infra genérica: `Specification`, `RegraNegocio`, `ValidadorRegras` | — |
| `domain.specification.pagamento` | Regras concretas + catálogo `RegrasProcessoPagamento` | — |
| `domain.state` | Um estado por classe (`EstadoAberto`, `EstadoEmProcessamento`...) | — |
| `domain.valueobject` | `Dinheiro`, `IdentificadorCota`, `DadosPagamento`, `Motivo`... (auto-validados) | — |
| `domain.model` | `Cota` (dado externo), `SolicitacaoPagamento` (candidato das specs) | — |
| `domain.factory` | Monta a solicitação com relógio e parâmetros de config | — |
| `domain.repository` | **Portas** (interfaces) | — |
| `adapter.database` | Implementa a porta com Panache; entidade JPA separada + mapper | domain |
| `adapter.cota` | Stub do sistema dono das cotas | domain |
| `shared` | `Pagina<T>`, producers CDI | — |

## Quem decide o quê

| Pergunta | Quem responde | Erro | HTTP |
|---|---|---|---|
| O JSON veio completo? | `entrypoint` (Bean Validation) | `ConstraintViolation` | 400 |
| O dado tem formato válido? (ex.: 3 casas decimais) | value objects | `RegraNegocioException` | 422 |
| **Essa ação é permitida no status atual?** | **State** (`domain.state`) | `TransicaoEstadoInvalidaException` | 409 |
| **Os dados atendem às regras de negócio?** | **Specifications** | `RegraNegocioException` (com **todas** as violações) | 422 |
| O recurso existe? | repositório (`obter`) | `RecursoNaoEncontradoException` | 404 |

A **entidade** é quem garante que estado e specifications sejam sempre aplicados:
não existe caminho para abrir/alterar sem validar nem mudar o status sem passar pelo estado.

## Specification pattern

```java
// bloco de construção: só diz sim/não
record FormaPagamentoEhSpec(FormaPagamento forma) implements Specification<SolicitacaoPagamento>

// regra em classe própria: sim/não + código + mensagem
class ValorPositivoSpec extends RegraNegocio<SolicitacaoPagamento>

// regra composta no catálogo
RegraNegocio.de("LIMITE_PIX_EXCEDIDO",
        quando(EH_PIX).entao(new ValorAteSpec(LIMITE_PIX)),
        s -> "Pagamento via PIX limitado a " + LIMITE_PIX);

// conjuntos
ABERTURA  = COMUNS.mais(new CotaSemProcessoEmAndamentoSpec());
ALTERACAO = COMUNS;
```

- Specs são **funções puras** sobre `SolicitacaoPagamento`. Fatos que exigem I/O
  (ex.: "existe processo em andamento?") são consultados pelo use case e **entregues** na solicitação.
- `ValidadorRegras` avalia **todas** as regras e lança uma exceção só, com a lista de violações:

```json
{ "codigo": "REGRAS_DE_NEGOCIO_VIOLADAS",
  "violacoes": [ {"codigo":"LIMITE_PIX_EXCEDIDO", ...},
                 {"codigo":"VENCIMENTO_EM_DIA_NAO_UTIL", ...},
                 {"codigo":"CONTA_DEBITO_INDEVIDA", ...} ] }
```

**Para criar uma regra nova:** crie a classe em `domain.specification.pagamento` (ou componha blocos),
adicione-a no catálogo `RegrasProcessoPagamento` e escreva o teste em `RegrasProcessoPagamentoTest`.
Nenhum use case muda.

## State pattern

```
ABERTO ──enviar──▶ EM_PROCESSAMENTO ──confirmar──▶ CONFIRMADO
  │  ▲                    │
  │  └────reabrir──── REJEITADO ◀──rejeitar──┘
  │                       │
  └─cancelar─▶ CANCELADO ◀─cancelar─┘
```

| Estado | Ações permitidas | Alterar/Excluir | Em andamento |
|---|---|---|---|
| ABERTO | enviar, cancelar | sim | sim |
| EM_PROCESSAMENTO | confirmar, rejeitar | não | sim |
| REJEITADO | reabrir, cancelar | não | sim |
| CONFIRMADO | — | não | não |
| CANCELADO | — | não | não |

- `EstadoProcessoPagamento` é `sealed` e **proíbe tudo por padrão**; cada estado sobrescreve só o que permite.
- As transições **retornam o próximo estado**; a entidade faz `estado = estado.confirmar()`. Sem setter.
- O enum `StatusProcessoPagamento` é só o rótulo (para banco/API). `EstadoProcessoPagamento.de(status)`
  converte; o `switch` não compila se faltar um estado.
- "Em andamento" é definido pelos estados e reaproveitado pelo adapter na query, sem duplicar a regra.

**Para criar um estado novo:** adicione o valor no enum, crie a classe `final` em `domain.state`,
inclua-a no `permits` e no `switch` de `de()`, sobrescreva as transições que levam a ele
e atualize a tabela em `EstadoProcessoPagamentoTest`.

## API

| Método | Caminho | Ação |
|---|---|---|
| POST | `/api/v1/processos-pagamento` | criar |
| GET | `/api/v1/processos-pagamento?grupo=&cota=&status=&pagina=0&tamanho=20` | listar |
| GET | `/api/v1/processos-pagamento/{id}` | consultar |
| PUT | `/api/v1/processos-pagamento/{id}` | alterar (só ABERTO) |
| DELETE | `/api/v1/processos-pagamento/{id}` | excluir (só ABERTO; depois disso, cancele) |
| POST | `/{id}/envio` · `/{id}/confirmacao` · `/{id}/reabertura` | transições |
| POST | `/{id}/rejeicao` · `/{id}/cancelamento` (body `{"motivo": "..."}`) | transições com motivo |

Massa fake de cotas (grupo `1234`): `1..49` ativas, `90` inadimplente, `91` cancelada, `92` encerrada.

```bash
mvn -pl consorcio-pagamento quarkus:dev   # http://localhost:8081
```

## Regras fake implementadas

| Código | Regra |
|---|---|
| `COTA_NAO_ACEITA_PAGAMENTO` | cota cancelada/encerrada não paga (inadimplente pode) |
| `COTA_COM_PROCESSO_EM_ANDAMENTO` | um processo em andamento por cota (só na abertura) |
| `VALOR_NAO_POSITIVO` | valor > 0 |
| `VALOR_EXCEDE_SALDO_DEVEDOR` | valor ≤ saldo devedor |
| `VALOR_MINIMO_BOLETO` | boleto ≥ R$ 10,00 |
| `LIMITE_PIX_EXCEDIDO` | PIX ≤ R$ 5.000,00 |
| `VENCIMENTO_NO_PASSADO` | vencimento ≥ hoje |
| `VENCIMENTO_ALEM_DO_PRAZO` | vencimento ≤ hoje + N dias (`consorcio.pagamento.prazo-maximo-vencimento-dias`) |
| `VENCIMENTO_EM_DIA_NAO_UTIL` | vencimento não cai no fim de semana |
| `CONTA_DEBITO_OBRIGATORIA` | débito em conta exige conta |
| `CONTA_DEBITO_INDEVIDA` | conta só para débito em conta |

## Testes

| Teste | O que cobre | Quarkus? |
|---|---|---|
| `SpecificationTest` | and/or/not, `quando().entao()`, acúmulo de violações | não |
| `RegrasProcessoPagamentoTest` | cada regra isolada, a partir de um cenário válido (`Cenarios`) | não |
| `EstadoProcessoPagamentoTest` | a máquina de estados inteira como tabela | não |
| `ProcessoPagamentoTest` | a entidade aplica estado (409) antes das regras (422) | não |
| `ProcessoPagamentoResourceTest` | CRUD + ciclo de estados + códigos HTTP ponta a ponta | sim |
