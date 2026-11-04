# pagamento-boleto — CRUD de referência

Simulação do ciclo de vida de um **boleto**: **edição por partes** (rascunho que pode ficar incompleto),
**emissão** (exige completude e gera a linha digitável), **notificação** ao pagador e **pagamento num
sistema externo**, que avisa por webhook; pagamentos fora do esperado vão para **análise** do financeiro,
que aceita ou devolve. Organizado em camadas, com:

- **domínio rico** (sem setters, sem anotações de framework);
- **Specification pattern** para regras de negócio (as regras são fake/hipotéticas);
- **State pattern** para a máquina de estados do boleto;
- **exceções específicas** por situação, agrupadas em famílias;
- **use cases finos**, um por intenção do usuário, que só orquestram (critério em
  ["O que é um caso de uso"](.github/copilot-instructions.md)).

## Camadas

```
entrypoint ──▶ application ──▶ domain
                    │             ▲
                    └─▶ adapter ──┘
```

As camadas se ligam por **injeção de dependência de classes concretas** (sem ports and adapters):
o use case injeta `BoletoRepository` e `NotificadorBoleto` direto de `adapter`. O domínio continua
sem nenhuma dependência de framework nem das outras camadas.

| Pacote | Papel | Pode depender de |
|---|---|---|
| `entrypoint.webhook.v1` | Entrada do sistema externo de pagamentos: contrato DELE, token próprio | application |
| `entrypoint.api.v1` | Contrato HTTP próprio (`request/`, `response/`), validação **sintática** (`@NotNull`), `ApiExceptionHandler` | application |
| `application.boleto` | Um `*UC` por ação: busca fatos, chama o domínio, persiste, devolve DTO. `@Transactional` aqui | domain, adapter |
| `domain.entity` | `Boleto` (aggregate root) | — |
| `domain.specification` | Infra genérica: `Specification`, `RegraNegocio`, `ValidadorRegras` | — |
| `domain.specification.boleto` | Regras concretas, catálogo `RegrasBoleto` e candidatos por momento: `ItensBoleto`/`DestinatariosDoBoleto` (seções), `EmissaoBoleto` + `CondicoesEmissao` (emissão), `RecebimentoPagamento` (conformidade do pagamento) | — |
| `domain.state` | Um estado por classe (`EstadoEmitido`, `EstadoNotificado`...) + enum `StatusBoleto` | — |
| `domain.valueobject` | `Dinheiro`, `Documento`, `Pagador`, `LinhaDigitavel`, `Pagamento`... (auto-validados) | — |
| `domain.exception` | Exceções específicas, em quatro famílias | — |
| `adapter.database` | `BoletoRepository` (Panache) que recebe/devolve `Boleto`; entidade JPA separada + mapper; traduz lock otimista | domain |
| `adapter.notificacao` | `NotificadorBoleto`: notificação **simulada** (log + lista em memória) | domain |
| `adapter.pagamentos` | `SistemaPagamentos`: saída para o sistema externo (pede devoluções; **simulado**) | domain |
| `shared` | `Pagina<T>`, `ClockProducer` (sem subpastas). **Única exceção à regra de dependência**: pode ser usado por qualquer camada, então só entra aqui o que é genérico e estável | — |

A estrutura de pastas é **por tipo e enxuta**: sem `factory`, `model`, `enums`, `repository` ou
`service` no domínio. A justificativa e o mapa de quem chama quem estão em `.github/copilot-instructions.md`.

## Quem decide o quê

| Pergunta | Quem responde | Família da exceção | HTTP |
|---|---|---|---|
| O JSON veio completo? | `entrypoint` (Bean Validation) | `ConstraintViolation` | 400 |
| O dado tem formato válido? (CPF com 11 dígitos, 2 casas decimais) | value objects | `DadoInvalidoException` | 400 |
| O boleto existe? | repositório (`obter`) | `RecursoNaoEncontradoException` | 404 |
| **Essa ação é permitida no status atual?** | **State** (`domain.state`) | `TransicaoEstadoInvalidaException` | 409 |
| Outra requisição alterou o boleto antes? | adapter (lock otimista com `@Version`) | `AlteracaoConcorrenteException` | 409 |
| **Os dados atendem às regras de negócio?** | **Specifications** | `RegraNegocioException` | 422 |
| O aviso de pagamento (fato externo) está conforme? | **Specifications** em modo classificação | nenhuma: vira motivo de `EM_ANALISE` | 200 |

A **entidade** é quem garante que estado e specifications sejam sempre aplicados, sempre nesta ordem:
1º o estado diz se a ação é permitida; 2º as regras dizem se os dados valem; 3º só então o estado muda.

## Exceções

Cada situação tem a **sua** exceção, agrupada em quatro famílias abstratas:

```
DomainException (codigo + mensagem)
├── DadoInvalidoException            → 400   ValorMonetarioInvalido, DocumentoInvalido,
│                                            PagadorInvalido, MotivoInvalido
├── RecursoNaoEncontradoException    → 404   BoletoNaoEncontrado
├── ConflitoException                → 409   TransicaoEstadoInvalida (statusAtual + acao),
│                                            AlteracaoConcorrente (lock otimista)
└── RegraNegocioException            → 422   uma por regra: BoletoVencido, ValorPagoDivergente,
                                             LimiteBoletosEmAbertoExcedido, ...
```

- Cada exceção concreta tem `CODIGO` fixo (`BoletoVencidoException.CODIGO = "BOLETO_VENCIDO"`)
  e recebe os **dados** do erro no construtor; a mensagem é montada dentro dela.
- O `ApiExceptionHandler` decide o status pela **família**: exceção nova não exige mudar o handler.
- **Todo** erro sai no mesmo formato, inclusive os que não vêm do domínio: Bean Validation (com a lista
  `campos`), JSON mal formado, rota/UUID inexistente e erro inesperado (500 sem stack trace, com log).
- Erros de programação (ex.: tentar trocar o pagador de um boleto) **não** são `DomainException`:
  usam `IllegalArgumentException` e viram 500.
- `Documento.toString()` mascara o CPF/CNPJ, então ele nunca aparece inteiro em log ou mensagem de erro.

```json
{"codigo": "BOLETO_VENCIDO", "mensagem": "Boleto vencido em 2026-11-10 não aceita pagamento em 2026-11-12"}
{"codigo": "REQUISICAO_INVALIDA", "mensagem": "Campos inválidos na requisição",
 "campos": [{"campo": "valor", "mensagem": "must not be null"}]}
```

## Specification pattern

```java
// bloco de construção: só diz sim/não
record ValorAteSpec(Dinheiro maximo) implements Specification<EmissaoBoleto>

// regra em classe própria: sim/não + qual exceção lançar
class VencimentoEmDiaUtilSpec extends RegraNegocio<EmissaoBoleto> {
    public boolean isSatisfiedBy(EmissaoBoleto s) { ... }
    protected VencimentoEmDiaNaoUtilException violacao(EmissaoBoleto s) { ... }
}

// regra composta no catálogo
RegraNegocio.de(
        quando(new PagadorPessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PESSOA_FISICA)),
        s -> new LimitePessoaFisicaExcedidoException(LIMITE_PESSOA_FISICA));

// conjuntos POR MOMENTO (cada um sobre o seu candidato)
ITENS         = ValidadorRegras.de(...);   // ao salvar a seção de itens      -> ItensBoleto
DESTINATARIOS = ValidadorRegras.de(...);   // ao salvar a seção de destinat.  -> DestinatariosDoBoleto
EMISSAO       = ValidadorRegras.de(        // ao finalizar: COMPLETUDE        -> EmissaoBoleto
        new BoletoComItensSpec(), VALOR_ACIMA_DO_MINIMO, ..., new LimiteBoletosEmAbertoSpec());
CONFORMIDADE_PAGAMENTO = ValidadorRegras.de(...); // classifica o aviso externo -> RecebimentoPagamento
```

- **Regra de seção × regra de completude:** salvar uma seção só valida a própria seção; o boleto pode
  continuar incompleto em `EM_EDICAO`. A completude (ter itens, valor total dentro dos limites,
  vencimento válido no dia) só é exigida na emissão.
- Specs são **funções puras** sobre um candidato. Fatos que exigem I/O
  (ex.: "quantos boletos em aberto o pagador tem?") são consultados pelo use case e **entregues** no candidato.
- `ValidadorRegras` avalia na ordem do catálogo e **para na primeira regra violada** (fail-fast).

**Para criar uma regra nova:**
1. crie a exceção em `domain.exception` estendendo `RegraNegocioException`;
2. crie a spec em `domain.specification.boleto` (ou componha blocos no catálogo);
3. adicione-a em `RegrasBoleto`, no conjunto e na posição desejados;
4. teste com `assertThrows(SuaException.class, ...)` em `RegrasBoletoTest`.

Nenhum use case nem o handler HTTP mudam.

**Limite consciente:** a entidade usa o catálogo de forma estática, o que exige specs **puras**.
Se uma regra precisar de algo externo (calendário de feriados, limite vindo de outro sistema),
não injete dependência na spec: o use case consulta o dado e o entrega no candidato.

## State pattern

```
 EM_EDICAO ─emitir─▶ EMITIDO ─notificar─▶ NOTIFICADO (↺ reenvio)
                        │                     │
                        │  pagamento recebido (webhook do sistema externo)
                        ├── conforme ───────▶ PAGO
                        └── divergente ─────▶ EM_ANALISE ─aceitar─▶ PAGO
                        │                         └──────devolver──▶ DEVOLVIDO
                        └─cancelar─▶ CANCELADO ──pagamento recebido──▶ EM_ANALISE
```

| Estado | Ações permitidas | Editar seções/Excluir | Em aberto |
|---|---|---|---|
| EM_EDICAO | emitir | sim | não (não é cobrável) |
| EMITIDO | notificar, cancelar, receber pagamento (conforme → PAGO, divergente → EM_ANALISE) | não | sim |
| NOTIFICADO | notificar (reenvio), cancelar, receber pagamento (idem) | não | sim |
| EM_ANALISE | aceitar (→ PAGO), devolver (→ DEVOLVIDO) | não | não |
| CANCELADO | só recebe pagamento (sempre → EM_ANALISE) | não | não |
| PAGO, DEVOLVIDO | — (estados finais) | não | não |

- `EstadoBoleto` é `sealed` e **proíbe tudo por padrão**; cada estado sobrescreve só o que permite.
- As transições **retornam o próximo estado**; a entidade faz `estado = estado.cancelar()`. Sem setter.
  O reenvio da notificação é uma transição para o **próprio** estado (`return this`).
- O enum `StatusBoleto` é só o rótulo (para banco/API). `EstadoBoleto.de(status)` converte;
  o `switch` não compila se faltar um estado.
- "Em aberto" é definido pelos estados e reaproveitado pelo adapter na contagem por pagador.

**Para criar um estado novo:** adicione o valor no enum, crie a classe `final` em `domain.state`,
inclua-a no `permits` e no `switch` de `de()`, sobrescreva as transições que levam a ele
e atualize a tabela em `EstadoBoletoTest`.

## Edição por partes (um caso de uso por intenção)

O boleto tem **partes** (itens e destinatários extras da notificação) que podem ser preenchidas em
qualquer ordem, em requisições separadas, enquanto ele está em `EM_EDICAO`:

| Intenção do usuário | Caso de uso | Endpoint | Regras |
|---|---|---|---|
| Começar um boleto | `CriarBoletoUC` | `POST /boletos` | só formato (pagador, vencimento) |
| Mudar o vencimento | `AlterarBoletoUC` | `PUT /boletos/{id}` | — |
| Salvar os itens | `SalvarItensBoletoUC` | `PUT /boletos/{id}/itens` | seção `ITENS` |
| Salvar quem mais recebe o aviso | `SalvarDestinatariosBoletoUC` | `PUT /boletos/{id}/destinatarios` | seção `DESTINATARIOS` |
| Finalizar e emitir | `EmitirBoletoUC` | `POST /boletos/{id}/emissao` | `EMISSAO` (completude) |

- **Não** há um UC por item, nem por operação (incluir/alterar/remover item), nem um UC com vários
  métodos: cada **seção** é salva inteira (`PUT`), porque é assim que o usuário a edita.
- Itens e destinatários são **partes do agregado**: sem repositório nem UC próprios. O UC carrega o
  `Boleto`, chama `boleto.salvarItens(...)` e salva o boleto inteiro (`@ElementCollection` no adapter).
- O `@Version` está no boleto: duas edições concorrentes, mesmo de seções diferentes, não se sobrescrevem.
- O valor do boleto é a **soma dos itens**; a linha digitável só existe depois da emissão.

## Pagamento num sistema externo

O pagamento **não acontece aqui**: o pagador paga no sistema externo de pagamentos, que nos **avisa**
por webhook. É o exemplo das duas direções de integração:

| Direção | Quem chama | Onde fica | O que faz |
|---|---|---|---|
| Entrada | sistema externo → nós | `entrypoint/webhook/v1/PagamentoWebhookResource` | recebe o aviso de pagamento |
| Saída | nós → sistema externo | `adapter/pagamentos/SistemaPagamentos` | pede a devolução de um pagamento |

Quatro ideias, cada uma num lugar só:

1. **O contrato é do sistema externo, não nosso.** O aviso chega no formato dele (`valorPagoCentavos`,
   `dataHoraPagamento` com fuso). O `AvisoPagamentoRequest` traduz na borda; o domínio nunca vê
   centavos. O webhook é um entrypoint separado de `api.v1` (outro cliente, outra autenticação:
   header `X-Webhook-Token`), mas também só traduz e chama um use case.
2. **Aviso de pagamento é FATO, não COMANDO.** Um comando ("emita este boleto") pode ser recusado com
   422. Um fato ("o pagador pagou R$ 499") não: o dinheiro já entrou. Por isso as **mesmas regras**
   de conformidade são usadas de outro jeito: `ValidadorRegras.primeiraViolacao(...)` não lança, só
   diz qual regra não foi atendida, e isso vira o **motivo da análise** (`EM_ANALISE`).
3. **Idempotência.** Webhooks são reenviados quando a resposta se perde. O mesmo aviso (mesmo
   `idTransacao`) não muda nada e responde 200 de novo. Outra transação para boleto já pago é
   recusada pelo estado (409) e fica para conciliação manual (fora do escopo do exemplo).
4. **Decisão humana como caso de uso.** O financeiro resolve a análise com `AceitarPagamentoBoletoUC`
   ou `DevolverPagamentoBoletoUC`. Este último aciona o sistema externo **depois** de salvar
   (mesma ordem da notificação; risco restante resolvido em produção com Outbox).

## Notificação

`NotificarBoletoUC` (1) valida a transição no domínio, (2) salva, o que detecta concorrência antes de
qualquer efeito externo, e (3) só então chama o `NotificadorBoleto` (injetado direto do adapter),
que avisa o pagador e os destinatários extras: só escreve no log e guarda o que "enviou".
Em produção, a janela "enviou, mas o commit falhou" se resolve com o padrão **Outbox**.

## API

| Método | Caminho | Ação |
|---|---|---|
| POST | `/api/v1/boletos` | cria o rascunho (`EM_EDICAO`): `{"pagador": {...}, "dataVencimento": "..."}` |
| GET | `/api/v1/boletos?documentoPagador=&status=&pagina=0&tamanho=20` | listar |
| GET | `/api/v1/boletos/{id}` | consultar |
| PUT | `/api/v1/boletos/{id}` | alterar o vencimento (só `EM_EDICAO`) |
| PUT | `/api/v1/boletos/{id}/itens` | salvar a seção de itens: `{"itens": [{"descricao": "...", "valor": 500.00}]}` (só `EM_EDICAO`) |
| PUT | `/api/v1/boletos/{id}/destinatarios` | salvar destinatários extras: `{"emails": ["..."]}` (só `EM_EDICAO`) |
| DELETE | `/api/v1/boletos/{id}` | excluir o rascunho (só `EM_EDICAO`; depois de emitido, cancele) |
| POST | `/api/v1/boletos/{id}/emissao` | finaliza a edição: exige completude e gera a linha digitável |
| POST | `/api/v1/boletos/{id}/notificacao` | simula o envio (ou reenvio) da notificação ao pagador e destinatários |
| POST | `/api/v1/boletos/{id}/cancelamento` | cancela: `{"motivo": "..."}` |
| POST | `/api/v1/boletos/{id}/aceite-pagamento` | financeiro aceita o pagamento em análise (→ `PAGO`) |
| POST | `/api/v1/boletos/{id}/devolucao` | financeiro devolve o pagamento em análise: `{"motivo": "..."}` (→ `DEVOLVIDO`) |
| POST | `/webhooks/v1/pagamentos` | **chamado pelo sistema externo**: aviso de pagamento (header `X-Webhook-Token`) |

```bash
mvn quarkus:dev   # http://localhost:8081

B=localhost:8081/api/v1/boletos; J='Content-Type: application/json'
ID=$(curl -s -X POST $B -H "$J" -d '{"pagador": {"nome": "Maria Silva", "documento": "12345678901",
  "email": "maria@example.com"}, "dataVencimento": "2026-11-10"}' | jq -r .id)
curl -X PUT  $B/$ID/itens -H "$J" -d '{"itens": [{"descricao": "Mensalidade", "valor": 500.00}]}'
curl -X POST $B/$ID/emissao

# simula o SISTEMA EXTERNO avisando que o boleto foi pago (formato dele: centavos, data/hora com fuso)
LINHA=$(curl -s $B/$ID | jq -r .linhaDigitavel)
curl -X POST localhost:8081/webhooks/v1/pagamentos -H "$J" -H 'X-Webhook-Token: token-local-de-desenvolvimento' \
  -d "{\"idTransacao\": \"TX-1\", \"linhaDigitavel\": \"$LINHA\", \"valorPagoCentavos\": 49900,
       \"dataHoraPagamento\": \"2026-11-09T14:30:00-03:00\"}"     # R$ 499: vai para EM_ANALISE
curl -X POST $B/$ID/devolucao -H "$J" -d '{"motivo": "pago a menor"}'
```

## Regras fake implementadas

| Código (exceção `<Nome>Exception`) | Quando | Regra |
|---|---|---|
| `QUANTIDADE_MAXIMA_ITENS_EXCEDIDA` | seção itens | no máximo 20 itens |
| `ITEM_COM_VALOR_ZERADO` | seção itens | todo item com valor > 0 |
| `QUANTIDADE_MAXIMA_DESTINATARIOS_EXCEDIDA` | seção destinatários | no máximo 5 e-mails extras |
| `DESTINATARIO_REPETIDO` | seção destinatários | sem repetidos nem o e-mail do pagador |
| `BOLETO_SEM_ITENS` | emissão (completude) | ao menos um item |
| `VALOR_ABAIXO_DO_MINIMO` | emissão | valor total ≥ R$ 5,00 |
| `VALOR_ACIMA_DO_MAXIMO` | emissão | valor total ≤ R$ 100.000,00 |
| `LIMITE_PESSOA_FISICA_EXCEDIDO` | emissão | pagador com CPF: valor total ≤ R$ 10.000,00 |
| `VENCIMENTO_NO_PASSADO` | emissão | vencimento ≥ hoje |
| `VENCIMENTO_ALEM_DO_PRAZO` | emissão | vencimento ≤ hoje + N dias (`boleto.prazo-maximo-vencimento-dias`) |
| `VENCIMENTO_EM_DIA_NAO_UTIL` | emissão | vencimento não cai no fim de semana |
| `LIMITE_BOLETOS_EM_ABERTO_EXCEDIDO` | emissão | no máximo 3 boletos em aberto (emitidos/notificados) por pagador; rascunhos não contam |
| `BOLETO_NAO_ESTA_EM_ABERTO` | conformidade do pagamento | boleto pago precisa estar em aberto (pago após cancelar → análise) |
| `DATA_PAGAMENTO_FUTURA` | conformidade do pagamento | data do pagamento ≤ hoje |
| `BOLETO_VENCIDO` | conformidade do pagamento | data do pagamento ≤ vencimento (sem juros/multa) |
| `VALOR_PAGO_DIVERGENTE` | conformidade do pagamento | valor pago = valor do boleto |

As regras de **conformidade do pagamento** não geram erro: a primeira não atendida vira o motivo da
análise (`analise.codigo` na resposta), porque o pagamento é um fato que já aconteceu.

A linha digitável também é fake (47 dígitos, não segue o padrão FEBRABAN) e o CPF/CNPJ só tem o tamanho validado.

## Testes

| Teste | O que cobre | Quarkus? |
|---|---|---|
| `SpecificationTest` | and/or/not, `quando().entao()`, fail-fast (`validar`) e classificação (`primeiraViolacao`) | não |
| `RegrasBoletoTest` | cada regra lança/indica sua exceção, a partir de um cenário válido (`Cenarios`) | não |
| `EstadoBoletoTest` | a máquina de estados inteira como tabela | não |
| `BoletoTest` | edição por partes, completude na emissão, pagamento como fato (análise, idempotência), estado (409) antes das regras (422) | não |
| `CondicoesEmissaoTest` | "hoje" no fuso do negócio, não no do servidor | não |
| `ConcorrenciaTest` | edições concorrentes de seções diferentes: lock otimista vira `AlteracaoConcorrenteException` | sim |
| `BoletoResourceTest` | edição por partes, emissão, webhook do sistema externo (token, idempotência, análise, devolução), códigos HTTP | sim |

## Assistentes de código (GitHub Copilot)

As regras deste README também estão em `.github/`, para o Copilot segui-las automaticamente:

| Arquivo | Quando é usado |
|---|---|
| `.github/copilot-instructions.md` | Em toda requisição: arquitetura, regras inegociáveis, comandos |
| `.github/instructions/<camada>.instructions.md` | Ao mexer em arquivos da camada (`applyTo` por caminho) |
| `.github/skills/<skill>/SKILL.md` | Quando a tarefa combina com a skill (ou ao pedir pelo nome) |

Skills: `nova-regra-de-negocio`, `novo-estado`, `novo-caso-de-uso`, `novo-agregado-crud`,
`nova-integracao-externa` e `revisao-arquitetura`. Esta última inclui
`.github/skills/revisao-arquitetura/verificar-camadas.sh`, que falha se alguma camada importar o que
não deve ou se a entidade ganhar setter, construtor público, `now()` sem `Clock` ou `if` em status.

Versões **genéricas** dessas skills e instruções, para usar em outros projetos (com o modelo de
"Mapa do projeto" e os templates da base genérica), estão em [`padroes/`](padroes/README.md).
