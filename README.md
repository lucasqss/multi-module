# pagamento-boleto — CRUD de referência

Simulação do ciclo de vida de um **boleto**: **emissão** (gera a linha digitável), **notificação**
ao pagador e **recebimento do pagamento** informado pelo banco. Organizado em camadas, com:

- **domínio rico** (sem setters, sem anotações de framework);
- **Specification pattern** para regras de negócio (as regras são fake/hipotéticas);
- **State pattern** para a máquina de estados do boleto;
- **exceções específicas** por situação, agrupadas em famílias;
- **use cases finos**, um por ação, que só orquestram.

## Camadas

```
entrypoint ──▶ application ──▶ domain ◀── adapter
 (HTTP)        (use cases)    (regras)    (JPA, notificação)
```

| Pacote | Papel | Pode depender de |
|---|---|---|
| `entrypoint.api.v1` | Contrato HTTP próprio (`request/`, `response/`), validação **sintática** (`@NotNull`), `ApiExceptionHandler` | application |
| `application.boleto` | Um `*UC` por ação: busca fatos, chama o domínio, persiste, devolve DTO. `@Transactional` aqui | domain |
| `domain.entity` | `Boleto` (aggregate root) | — |
| `domain.specification` | Infra genérica: `Specification`, `RegraNegocio`, `ValidadorRegras` | — |
| `domain.specification.boleto` | Regras concretas + catálogo `RegrasBoleto` | — |
| `domain.state` | Um estado por classe (`EstadoEmitido`, `EstadoNotificado`...) | — |
| `domain.valueobject` | `Dinheiro`, `Documento`, `Pagador`, `LinhaDigitavel`, `Pagamento`... (auto-validados) | — |
| `domain.model` | Candidatos das regras: `SolicitacaoBoleto`, `RecebimentoPagamento` | — |
| `domain.factory` | `SolicitacaoBoletoFactory`: monta a solicitação com relógio (fuso do negócio) e config | — |
| `domain.repository` | **Portas** de persistência | — |
| `domain.gateway` | **Portas** de integração externa (`NotificadorBoleto`) | — |
| `domain.exception` | Exceções específicas, em quatro famílias | — |
| `adapter.database` | Implementa a porta com Panache; entidade JPA separada + mapper; traduz lock otimista | domain |
| `adapter.notificacao` | Notificação **simulada** (log + lista em memória) | domain |
| `shared` | `Pagina<T>`, producers CDI. **Única exceção à regra de dependência**: pode ser usado por qualquer camada, então só entra aqui o que é genérico e estável | — |

## Quem decide o quê

| Pergunta | Quem responde | Família da exceção | HTTP |
|---|---|---|---|
| O JSON veio completo? | `entrypoint` (Bean Validation) | `ConstraintViolation` | 400 |
| O dado tem formato válido? (CPF com 11 dígitos, 2 casas decimais) | value objects | `DadoInvalidoException` | 400 |
| O boleto existe? | repositório (`obter`) | `RecursoNaoEncontradoException` | 404 |
| **Essa ação é permitida no status atual?** | **State** (`domain.state`) | `TransicaoEstadoInvalidaException` | 409 |
| Outra requisição alterou o boleto antes? | adapter (lock otimista com `@Version`) | `AlteracaoConcorrenteException` | 409 |
| **Os dados atendem às regras de negócio?** | **Specifications** | `RegraNegocioException` | 422 |

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
record ValorAteSpec(Dinheiro maximo) implements Specification<SolicitacaoBoleto>

// regra em classe própria: sim/não + qual exceção lançar
class VencimentoEmDiaUtilSpec extends RegraNegocio<SolicitacaoBoleto> {
    public boolean isSatisfiedBy(SolicitacaoBoleto s) { ... }
    protected VencimentoEmDiaNaoUtilException violacao(SolicitacaoBoleto s) { ... }
}

// regra composta no catálogo
RegraNegocio.de(
        quando(new PagadorPessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PESSOA_FISICA)),
        s -> new LimitePessoaFisicaExcedidoException(LIMITE_PESSOA_FISICA));

// conjuntos (cada um sobre um tipo de candidato)
EMISSAO     = COMUNS.mais(new LimiteBoletosEmAbertoSpec());   // SolicitacaoBoleto
ALTERACAO   = COMUNS;                                         // SolicitacaoBoleto
RECEBIMENTO = ValidadorRegras.de(...);                        // RecebimentoPagamento
```

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
           notificar             receber pagamento
  EMITIDO ───────────▶ NOTIFICADO ─────────────────▶ PAGO
     │  ╰──────────── receber pagamento ─────────────▲
     │                     │  ↺ notificar (reenvio)
     └──── cancelar ───────┴──── cancelar ────▶ CANCELADO
```

| Estado | Ações permitidas | Alterar/Excluir | Em aberto |
|---|---|---|---|
| EMITIDO | notificar, receber pagamento, cancelar | sim | sim |
| NOTIFICADO | notificar (reenvio), receber pagamento, cancelar | não | sim |
| PAGO | — | não | não |
| CANCELADO | — | não | não |

- `EstadoBoleto` é `sealed` e **proíbe tudo por padrão**; cada estado sobrescreve só o que permite.
- As transições **retornam o próximo estado**; a entidade faz `estado = estado.cancelar()`. Sem setter.
  O reenvio da notificação é uma transição para o **próprio** estado (`return this`).
- O enum `StatusBoleto` é só o rótulo (para banco/API). `EstadoBoleto.de(status)` converte;
  o `switch` não compila se faltar um estado.
- "Em aberto" é definido pelos estados e reaproveitado pelo adapter na contagem por pagador.

**Para criar um estado novo:** adicione o valor no enum, crie a classe `final` em `domain.state`,
inclua-a no `permits` e no `switch` de `de()`, sobrescreva as transições que levam a ele
e atualize a tabela em `EstadoBoletoTest`.

## Notificação

`NotificarBoletoUC` (1) valida a transição no domínio, (2) salva, o que detecta concorrência antes de
qualquer efeito externo, e (3) só então chama a porta `NotificadorBoleto`. O adapter
`NotificadorBoletoFake` só escreve no log e guarda o que "enviou".
Em produção, a janela "enviou, mas o commit falhou" se resolve com o padrão **Outbox**.

## API

| Método | Caminho | Ação |
|---|---|---|
| POST | `/api/v1/boletos` | emitir |
| GET | `/api/v1/boletos?documentoPagador=&status=&pagina=0&tamanho=20` | listar |
| GET | `/api/v1/boletos/{id}` | consultar |
| PUT | `/api/v1/boletos/{id}` | alterar valor/vencimento (só EMITIDO; regera a linha digitável) |
| DELETE | `/api/v1/boletos/{id}` | excluir (só EMITIDO; depois disso, cancele) |
| POST | `/api/v1/boletos/{id}/notificacao` | simula o envio (ou reenvio) da notificação ao pagador |
| POST | `/api/v1/boletos/{id}/pagamento` | simula o banco avisando o pagamento: `{"valorPago": 500.00, "dataPagamento": "2026-11-10"}` |
| POST | `/api/v1/boletos/{id}/cancelamento` | cancela: `{"motivo": "..."}` |

```bash
mvn quarkus:dev   # http://localhost:8081

curl -X POST localhost:8081/api/v1/boletos -H 'Content-Type: application/json' -d '{
  "pagador": {"nome": "Maria Silva", "documento": "12345678901", "email": "maria@example.com"},
  "valor": 500.00, "dataVencimento": "2026-11-10"}'
```

## Regras fake implementadas

| Código (exceção `<Nome>Exception`) | Quando | Regra |
|---|---|---|
| `VALOR_ABAIXO_DO_MINIMO` | emissão/alteração | valor ≥ R$ 5,00 |
| `VALOR_ACIMA_DO_MAXIMO` | emissão/alteração | valor ≤ R$ 100.000,00 |
| `LIMITE_PESSOA_FISICA_EXCEDIDO` | emissão/alteração | pagador com CPF: valor ≤ R$ 10.000,00 |
| `VENCIMENTO_NO_PASSADO` | emissão/alteração | vencimento ≥ hoje |
| `VENCIMENTO_ALEM_DO_PRAZO` | emissão/alteração | vencimento ≤ hoje + N dias (`boleto.prazo-maximo-vencimento-dias`) |
| `VENCIMENTO_EM_DIA_NAO_UTIL` | emissão/alteração | vencimento não cai no fim de semana |
| `LIMITE_BOLETOS_EM_ABERTO_EXCEDIDO` | emissão | no máximo 3 boletos em aberto por pagador |
| `DATA_PAGAMENTO_FUTURA` | recebimento | data do pagamento ≤ hoje |
| `BOLETO_VENCIDO` | recebimento | data do pagamento ≤ vencimento (sem juros/multa) |
| `VALOR_PAGO_DIVERGENTE` | recebimento | valor pago = valor do boleto |

A linha digitável também é fake (47 dígitos, não segue o padrão FEBRABAN) e o CPF/CNPJ só tem o tamanho validado.

## Testes

| Teste | O que cobre | Quarkus? |
|---|---|---|
| `SpecificationTest` | and/or/not, `quando().entao()`, fail-fast do validador | não |
| `RegrasBoletoTest` | cada regra lança sua exceção, a partir de um cenário válido (`Cenarios`) | não |
| `EstadoBoletoTest` | a máquina de estados inteira como tabela | não |
| `BoletoTest` | a entidade aplica estado (409) antes das regras (422); linha digitável | não |
| `SolicitacaoBoletoFactoryTest` | "hoje" no fuso do negócio, não no do servidor | não |
| `ConcorrenciaTest` | lock otimista vira `AlteracaoConcorrenteException` | sim |
| `BoletoResourceTest` | CRUD + ciclo emitir/notificar/pagar + códigos HTTP + formato único de erro | sim |
