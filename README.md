# pagamento-boleto — sistema de referência

**Processo de pagamento de boletos.** O boleto **não é nosso**: o ciclo de vida dele (emissão,
alteração, cancelamento) pertence a um sistema externo, o **emissor**. O que é nosso é o **processo**:
montar o pagamento (boletos, conta, data), aprovar conforme a alçada, enviar ao **banco** (outro
sistema externo) e acompanhar o retorno. Organizado em camadas, com:

- **domínio rico** (sem setters, sem anotações de framework), com **entidades internas** no agregado;
- **Specification pattern** para regras de negócio (as regras são fake/hipotéticas);
- **State pattern** para a máquina de estados do processo;
- **exceções específicas** por situação, agrupadas em famílias;
- **use cases finos**, um por intenção, que só orquestram (critério em
  ["O que é um caso de uso"](.github/copilot-instructions.md));
- **dois sistemas externos**, cada um nos dois sentidos (consulta/envio e webhook).

## Visão geral

```
 Sistema EMISSOR (externo)                 Nosso sistema                         BANCO (externo)
 ┌─────────────────────────┐  consulta   ┌──────────────────────────┐  envio   ┌──────────────┐
 │ dono do boleto          │◀────────────│     ProcessoPagamento     │────────▶│ liquidação   │
 │ (emitir, cancelar...)   │──webhook───▶│     (nosso agregado)      │◀webhook─│ (retorno)    │
 └─────────────────────────┘  cancelado  └──────────────────────────┘ retorno  └──────────────┘
```

## Camadas

```
entrypoint ──▶ application ──▶ domain
                    │             ▲
                    └─▶ adapter ──┘
```

As camadas se ligam por **injeção de dependência de classes concretas** (sem ports and adapters):
os use cases injetam o repositório e as integrações direto de `adapter`. O domínio não depende de
nenhuma outra camada nem de framework.

| Pacote | Papel | Pode depender de |
|---|---|---|
| `entrypoint.api.v1` | Nossa API: contrato HTTP próprio (`request/`, `response/`), validação **sintática**, `ApiExceptionHandler` | application |
| `entrypoint.webhook.v1` | Entrada dos sistemas externos (banco, emissor): contrato e token DELES | application |
| `application.processo` | Um `*UC` por intenção: busca fatos, chama o domínio, persiste, devolve DTO | domain, adapter |
| `domain.entity` | `ProcessoPagamento` (raiz) e as entidades internas `Aprovacao`, `TentativaPagamento` | — |
| `domain.valueobject` | `BoletoAPagar` (cópia do boleto do emissor), `BoletosDoProcesso`, `ContaDebito`, `Matricula`, `Dinheiro`... | — |
| `domain.specification` | Base genérica: `Specification`, `RegraNegocio`, `ValidadorRegras` | — |
| `domain.specification.processo` | Regras concretas, catálogo `RegrasProcessoPagamento` e candidatos por momento | — |
| `domain.state` | Um estado por classe (`EstadoEmEdicao`, `EstadoAguardandoAprovacao`...) + enum `StatusProcesso` | — |
| `domain.exception` | Exceções específicas, em quatro famílias | — |
| `adapter.database` | `ProcessoPagamentoRepository` (Panache) que recebe/devolve o agregado; entidade JPA + mapper | domain |
| `adapter.emissor` | `EmissorBoletos`: consulta o boleto no emissor (**simulado**) | domain |
| `adapter.banco` | `BancoPagamentos`: envia o pagamento ao banco (**simulado**) | domain |
| `shared` | `Pagina<T>`, `ClockProducer` (sem subpastas). Pode ser usado por qualquer camada | — |

## O agregado e suas partes

| Elemento | Tipo | Por quê |
|---|---|---|
| `ProcessoPagamento` | **raiz** | Guarda o estado, as regras do conjunto e o `@Version` |
| `BoletoAPagar` (id no emissor, linha, valor, vencimento, beneficiário) | value object | **Cópia** do boleto do emissor: não é nosso; mudanças chegam como fato |
| `BoletosDoProcesso`, `ContaDebito` | value objects | Seções substituídas inteiras |
| `Aprovacao` (aprovador, momento) | **entidade interna** | Identidade (o aprovador) e histórico; a alçada pode exigir duas |
| `TentativaPagamento` (nº, id da transação, status, autenticação/recusa) | **entidade interna** | Ciclo de vida próprio (ENVIADA → LIQUIDADA/RECUSADA); pode haver várias |

Nenhuma parte tem repositório ou caso de uso próprio: tudo passa pela raiz, que é gravada inteira
(as partes viram `@ElementCollection` no adapter).

## Quem decide o quê

| Pergunta | Quem responde | Família da exceção | HTTP |
|---|---|---|---|
| O JSON veio completo? | `entrypoint` (Bean Validation) | `ConstraintViolation` | 400 |
| O dado tem formato válido? (matrícula, linha digitável, conta) | value objects | `DadoInvalidoException` | 400 |
| O processo existe? | repositório (`obter`) | `RecursoNaoEncontradoException` | 404 |
| **Essa ação é permitida no status atual?** | **State** (`domain.state`) | `TransicaoEstadoInvalidaException` | 409 |
| Outra requisição alterou o processo antes? | adapter (lock otimista) | `AlteracaoConcorrenteException` | 409 |
| **Os dados atendem às regras de negócio?** | **Specifications** | `RegraNegocioException` | 422 |
| A alçada foi atingida? | **Specification** como decisão (sem erro) | — | — |
| O retorno do banco (fato) está conforme? | **Specifications** em modo classificação | nenhuma: vira motivo de `EM_ANALISE` | 200 |

A **raiz** garante que estado e regras sejam sempre aplicados, nesta ordem: 1º o estado diz se a ação
é permitida; 2º as regras dizem se os dados valem (ou classificam o fato); 3º só então o estado muda.

## Exceções

```
DomainException (codigo + mensagem)
├── DadoInvalidoException            → 400   ValorMonetarioInvalido, MatriculaInvalida, LinhaDigitavelInvalida,
│                                            ContaDebitoInvalida, MotivoInvalido
├── RecursoNaoEncontradoException    → 404   ProcessoPagamentoNaoEncontrado
├── ConflitoException                → 409   TransicaoEstadoInvalida (statusAtual + acao),
│                                            AlteracaoConcorrente (lock otimista)
└── RegraNegocioException            → 422   uma por regra: ProcessoSemBoletos, AprovadorRepetido,
                                             BoletoVenceAntesDoPagamento, BoletoIndisponivelNoEmissor, ...
```

- Cada exceção concreta tem `CODIGO` fixo e recebe os **dados** do erro no construtor.
- O `ApiExceptionHandler` decide o status pela **família**: exceção nova não exige mudar o handler.
- **Todo** erro sai no mesmo formato (`{"codigo", "mensagem", "campos"?}`), inclusive Bean Validation,
  JSON mal formado, rota inexistente, token de webhook inválido (401) e erro inesperado (500 sem stack trace).
- Erro de programação **não** é `DomainException`: usa `IllegalArgumentException`/`IllegalStateException`.

## Specification pattern

As regras ficam no catálogo `RegrasProcessoPagamento`, agrupadas pelo **momento** em que valem:

```java
BOLETOS              // ao salvar a seção de boletos      -> BoletosDoProcesso
SUBMISSAO            // ao finalizar a edição: COMPLETUDE -> SubmissaoProcesso
APROVACAO            // a cada aprovação                  -> AprovacaoProcesso
ALCADA_ATINGIDA      // DECISÃO, sem erro: o estado escolhe o destino
        = quando(new ValorAcimaDeSpec(LIMITE_UMA_APROVACAO)).entao(new AprovacoesNoMinimoSpec(2));
CONFORMIDADE_RETORNO // CLASSIFICA o retorno do banco      -> RetornoPagamento
```

Três usos da mesma ferramenta:
- **Comando** (`validar`): o usuário pede algo; regra violada → 422.
- **Decisão** (`isSatisfiedBy`): a alçada não é erro; diz se o processo vai para `APROVADO` ou continua
  aguardando outra aprovação.
- **Fato** (`primeiraViolacao`): o banco já liquidou; não dá para recusar. A regra não atendida vira o
  motivo de `EM_ANALISE`.

Specs são **funções puras**. Dados que exigem I/O (o boleto no emissor, "hoje") são obtidos pelo use
case e entregues no candidato.

## State pattern

```
 EM_EDICAO ─submeter─▶ AGUARDANDO_APROVACAO ─aprovar (alçada atingida)─▶ APROVADO
    ▲                     │  ↺ aprovar (falta aprovação)                     │ enviar ao banco
    └──reabrir── REJEITADO ◀──rejeitar                                       ▼
                                  FALHOU ◀── retorno: recusado ──── EM_PROCESSAMENTO ── retorno: liquidado ──▶ PAGO
                                    └──── retentar ───────────────────────▲      └── liquidado ≠ total ──▶ EM_ANALISE

 Boleto cancelado no EMISSOR (fato): antes do envio → CANCELADO; depois do envio → EM_ANALISE.
 cancelar (usuário): qualquer estado não final, exceto EM_PROCESSAMENTO (o dinheiro pode já ter saído).
```

| Estado | Ações do usuário | Fatos externos | Editar/Excluir |
|---|---|---|---|
| EM_EDICAO | submeter, cancelar | boleto cancelado → CANCELADO | sim |
| AGUARDANDO_APROVACAO | aprovar, rejeitar, cancelar | boleto cancelado → CANCELADO | não |
| REJEITADO | reabrir, cancelar | boleto cancelado → CANCELADO | não |
| APROVADO | enviar, cancelar | boleto cancelado → CANCELADO | não |
| EM_PROCESSAMENTO | — | retorno (PAGO / FALHOU / EM_ANALISE); boleto cancelado → EM_ANALISE | não |
| FALHOU | retentar, cancelar | boleto cancelado → CANCELADO | não |
| PAGO | — | boleto cancelado → EM_ANALISE | não |
| EM_ANALISE | cancelar | registra, continua em análise | não |
| CANCELADO | — | ignorado | não |

## Casos de uso (um por intenção)

| Intenção | Caso de uso | Endpoint | Regras |
|---|---|---|---|
| Começar um pagamento | `CriarProcessoPagamentoUC` | `POST /processos-pagamento` | formato |
| Escolher os boletos | `SalvarBoletosDoProcessoUC` | `PUT /{id}/boletos` | consulta o **emissor**; seção `BOLETOS` |
| Definir conta e data | `AlterarProcessoPagamentoUC` | `PUT /{id}` | formato |
| Mandar para aprovação | `SubmeterProcessoPagamentoUC` | `POST /{id}/submissao` | `SUBMISSAO` (completude) |
| Aprovar / rejeitar | `AprovarProcessoPagamentoUC`, `RejeitarProcessoPagamentoUC` | `POST /{id}/aprovacoes`, `/rejeicao` | `APROVACAO` + alçada |
| Reabrir para correção | `ReabrirProcessoPagamentoUC` | `POST /{id}/reabertura` | — |
| Enviar ao banco | `EnviarProcessoParaPagamentoUC` | `POST /{id}/envio` (na vida real, um job) | — (chama o **banco** depois de salvar) |
| Tentar de novo | `RetentarPagamentoUC` | `POST /{id}/retentativa` | — |
| Cancelar | `CancelarProcessoPagamentoUC` | `POST /{id}/cancelamento` | — |
| Consultar / listar / excluir rascunho | `Consultar…`, `Listar…`, `Excluir…` | `GET`, `GET /{id}`, `DELETE /{id}` | — |
| **Banco** informa o resultado | `ReceberRetornoBancoUC` | `POST /webhooks/v1/banco/retornos` | `CONFORMIDADE_RETORNO` (classifica) |
| **Emissor** cancelou um boleto | `ReceberCancelamentoBoletoUC` | `POST /webhooks/v1/emissor/boletos-cancelados` | o estado de cada processo decide |

Partes com identidade (`Aprovacao`, `TentativaPagamento`) **não** ganham caso de uso próprio: aprovar e
enviar passam pelo `ProcessoPagamento`.

## Sistemas externos

| Sistema | Sentido | Onde | Ideia que ilustra |
|---|---|---|---|
| Emissor | saída: consulta | `adapter/emissor/EmissorBoletos` | dados de outro sistema viram uma cópia nossa (`BoletoAPagar`) |
| Emissor | entrada: boleto cancelado | `entrypoint/webhook/v1/EmissorWebhookResource` | fato sobre algo que não é nosso; cada processo decide |
| Banco | saída: envio | `adapter/banco/BancoPagamentos` | efeito externo **depois** de salvar (Outbox em produção) |
| Banco | entrada: retorno | `entrypoint/webhook/v1/BancoWebhookResource` | fato classificado, idempotência pela tentativa, contrato dele (centavos, data/hora com fuso) |

Os dois adapters são **simulados**. O emissor decodifica a linha digitável num layout fake:
`9` + 3 dígitos do emissor + 25 de id + vencimento `yyyyMMdd` + valor em centavos (10 dígitos).
Linha começando com `0` = boleto indisponível.

```bash
mvn quarkus:dev   # http://localhost:8081

A=localhost:8081/api/v1/processos-pagamento; J='Content-Type: application/json'
ID=$(curl -s -X POST $A -H "$J" -d '{"solicitante": "F1234567"}' | jq -r .id)
curl -X PUT $A/$ID/boletos -H "$J" -d '{"linhasDigitaveis": ["90010000000000000000000000001202612310000050000"]}'
curl -X PUT $A/$ID -H "$J" -d '{"contaDebito": {"agencia": "1234", "conta": "998877"}, "dataPagamento": "2026-12-01"}'
curl -X POST $A/$ID/submissao
curl -X POST $A/$ID/aprovacoes -H "$J" -d '{"aprovador": "A1111111"}'
TX=$(curl -s -X POST $A/$ID/envio | jq -r '.tentativas[-1].idTransacao')

# simula o BANCO avisando o resultado (formato dele: centavos, data/hora com fuso)
curl -X POST localhost:8081/webhooks/v1/banco/retornos -H "$J" -H 'X-Webhook-Token: token-banco-local' \
  -d "{\"idTransacao\": \"$TX\", \"situacao\": \"LIQUIDADO\", \"valorLiquidadoCentavos\": 50000,
       \"autenticacao\": \"AUT-1\", \"dataHora\": \"2026-12-01T10:15:00-03:00\"}"
```

## Regras fake implementadas

| Código | Momento | Regra |
|---|---|---|
| `BOLETO_INDISPONIVEL_NO_EMISSOR` | seção boletos | o emissor precisa ter o boleto em aberto |
| `QUANTIDADE_MAXIMA_BOLETOS_EXCEDIDA` | seção boletos | no máximo 20 boletos por processo |
| `BOLETO_REPETIDO_NO_PROCESSO` | seção boletos | o mesmo boleto não entra duas vezes |
| `PROCESSO_SEM_BOLETOS`, `CONTA_DEBITO_NAO_INFORMADA`, `DATA_PAGAMENTO_NAO_INFORMADA` | submissão | completude |
| `VALOR_TOTAL_ACIMA_DO_LIMITE` | submissão | soma ≤ R$ 500.000,00 |
| `DATA_PAGAMENTO_NO_PASSADO`, `DATA_PAGAMENTO_EM_DIA_NAO_UTIL` | submissão | data ≥ hoje, em dia útil |
| `BOLETO_VENCE_ANTES_DO_PAGAMENTO` | submissão | nenhum boleto vence antes da data de pagamento |
| `APROVADOR_IGUAL_AO_SOLICITANTE`, `APROVADOR_REPETIDO` | aprovação | segregação de funções; uma aprovação por pessoa |
| (alçada) | aprovação | acima de R$ 10.000,00 exige duas aprovações |
| `VALOR_LIQUIDADO_DIVERGENTE` | retorno do banco | classifica: liquidado ≠ total → `EM_ANALISE` |
| `BOLETO_CANCELADO_NO_EMISSOR` | aviso do emissor | classifica: depois do envio → `EM_ANALISE` |

## Testes

| Teste | O que cobre | Quarkus? |
|---|---|---|
| `SpecificationTest` | and/or/not, `quando().entao()`, `validar` e `primeiraViolacao` | não |
| `RegrasProcessoPagamentoTest` | cada regra, um `@Nested` por momento (seção, submissão, aprovação/alçada, conformidade) | não |
| `EstadoProcessoTest` | a máquina de estados inteira como tabela (9 estados × 12 ações) | não |
| `ProcessoPagamentoTest` | partes, entidades internas, alçada, tentativas, fatos do banco e do emissor, idempotência | não |
| `ConcorrenciaTest` | edição concorrente de partes diferentes → `AlteracaoConcorrenteException` | sim |
| `ProcessoPagamentoResourceTest` | fluxo completo, webhooks (token, idempotência), erros HTTP | sim |

## Assistentes de código (GitHub Copilot)

As regras deste README também estão em `.github/`, para o Copilot segui-las automaticamente:

| Arquivo | Quando é usado |
|---|---|
| `.github/copilot-instructions.md` | Em toda requisição: arquitetura, regras inegociáveis, comandos |
| `.github/instructions/<camada>.instructions.md` | Ao mexer em arquivos da camada (`applyTo` por caminho) |
| `.github/skills/<skill>/SKILL.md` | Quando a tarefa combina com a skill (ou ao pedir pelo nome) |

As skills (`layered-*`) e as instruções por camada são **genéricas**: servem a qualquer projeto no
padrão e usam este repositório apenas como **ilustração do "to be"**. O único trecho específico deste
projeto é o "Mapa do projeto", no fim do `copilot-instructions.md`.

`layered-revisao-arquitetura` inclui `verificar-camadas.sh`, que falha se alguma camada importar o que
não deve, se um use case chamar outro, ou se a entidade ganhar setter, construtor público, `now()` sem
`Clock` ou `if` em status.

Como levar essas skills e instruções para outros projetos: [`docs/padroes.md`](docs/padroes.md).
