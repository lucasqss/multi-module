---
name: layered-crud
description: Padrão para projetos Java (Quarkus) em camadas (domain, application, adapter, entrypoint, shared) com domínio rico, ligados por injeção de dependência direta (sem ports and adapters) e com o mínimo de classes. Cobre entidade JPA rica, repositório Panache no domínio, VO, caso de uso (e Command para transições), Specification + Chain of Responsibility, State, exceções por família, integração externa (saída, webhook, comando × fato), lock pessimista, testes e revisão. Use ao criar ou alterar agregado, caso de uso, regra, estado, integração ou endpoint, ao iniciar um projeto no padrão e ao revisar código.
---

# CRUD layered com domínio rico

> **Sobre os exemplos.** As regras abaixo são genéricas. Os trechos **Ilustração** vêm do repositório de
> referência **pagamento-boleto** (`lucasqss/multi-module`) e só mostram o "to be": em outro projeto esses
> arquivos não existem. Use os nomes do **Mapa do projeto** (fim de `.github/copilot-instructions.md`);
> se ele não existir, siga a seção 10 antes de tudo.

**Princípio:** menos código e menos classes, **sem custo para o entendimento** (SOLID preservado). Quem mantém
deve achar cada coisa num lugar óbvio. Abstração, DTO, VO, record ou mapper só com **ganho real** (regra,
comportamento, contrato externo), nunca por ganho teórico.

## 1. Camadas e estrutura

```
<pacote.base>/
├── domain/        entity (JPA), repository (Panache), valueobject, exception, specification[/<agregado>], state
├── application/   <agregado>/  *UC  +  dto/  (+ comando/ quando houver Command)
├── adapter/       <sistema-externo>/   (só integrações com outros sistemas)
├── entrypoint/    api/v1/ (Resource, request/, response/, handler/)   webhook/v1/ (ou iib/v1)
└── shared/        utilitários técnicos SEM subpastas (Pagina, ClockProducer)
```

Dependências permitidas: `entrypoint → application → (domain, adapter)`; `adapter → domain`; `domain` não
depende de outra camada. Framework no `domain`: **só persistência** (JPA/Hibernate na entidade; Panache + CDI
no repositório); nada de HTTP, JSON, config ou transação.

- **Sem ports and adapters.** O UC injeta classes concretas: o repositório (`domain/repository`) e os clientes
  externos (`adapter/<sistema>`, que traduzem o contrato deles para VO/entidade). Interface só com mais de
  uma implementação **real**.
- **Quem chama quem:** Resource → UC → (repositório/integração, entidade) ; entidade → estado e regras.
  UC não chama UC; não existe `*Service`/`*Helper`/`*Utils` genérico.
- **Estrutura enxuta:** nada de `domain/{factory,model,enums,service}` nem subpastas em `shared/`; ao achar
  uma, mova e apague (status → `state/`; enums/records → `valueobject/`; fábrica → `criar`; utilitário → spec/VO).
- **DTO/mapper só entre entrypoint e application:** Request/Response (HTTP, versionado) e Input/Output.
  Quem converte: `Request.paraInput(...)` e `Response.de(output)` (entrypoint), `Output.de(entidade)` (application).
  Entre application, domínio e repositório passam a **entidade**, tipos simples e VOs com regra: sem
  JpaEntity separada, sem Mapper, sem objeto de filtro.
- **Entrypoint fino:** só traduz HTTP ⇄ UC. Bean Validation no Request só para o **sintático** (`@NotNull`,
  `@NotBlank`, `@Min`) → 400; formato com regra (linha digitável, conta) fica no VO.
  Rotas: `POST` cria → 201 + `Location`; seção = `PUT /{id}/<secao>` (inteira); transição =
  `POST /{id}/<substantivo>` (`/submissao`, `/cancelamento`); fato externo só por `webhook/v1`.

## 2. Entidade JPA rica e value objects

- A entidade de domínio **é** a entidade JPA (`@Entity`), com acesso por campo e campos `private`.
  Construtor `protected` sem argumentos **só para o JPA**; o de negócio é `criar(...)` (aplica regras do
  nascimento). **Sem setters**: só métodos de negócio com nome do domínio. Nada de `PanacheEntity` (active
  record expõe `persist()`/finders na entidade).
- A entidade carregada é **gerenciada**: as mudanças saem no commit. Coleção (`@ElementCollection`) nunca é
  trocada: `clear()` + `addAll()`; o getter devolve o VO da seção.
- Ordem dentro de todo método que muda estado: **1º estado autoriza (409) → 2º regras validam (422)
  → 3º muda**. Se algo lança, nada mudou.
- Conceito com regra de formato ou comportamento vira **VO** (record imutável que se valida no construtor e
  lança `DadoInvalidoException` específica → 400). VO que só embrulha um tipo (ex.: id `UUID`) **não** vale.
  Persistência do VO: vários campos = `@Embeddable record` (+ `@AttributeOverride` na entidade); um campo =
  `AttributeConverter` com `autoApply`, aninhado no próprio VO (`Dinheiro.Conversor`).
  Tempo sempre via `Clock` injetado (nunca `now()` sem clock).
- Agregado editado por partes nasce `EM_EDICAO` e pode ficar incompleto; **só a finalização exige completude**.

Ilustração: `domain/entity/Pagamento`:

```java
@Entity @Table(name = "pagamento")
public class Pagamento {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) private StatusPagamento status;          // State (enum)
    @ElementCollection @BatchSize(size = 100) private List<BoletoAPagar> boletos = new ArrayList<>();
    @Embedded private ContaDebito contaDebito;                            // @Embeddable record
    protected Pagamento() { }                                             // só JPA

    public void submeter(LocalDate hoje) {
        var proximo = status.submeter();                                  // 409 se não pode
        RegrasPagamento.SUBMISSAO.validar(new SubmissaoPagamento(...));   // 422 se viola
        status = proximo;                                                 // só então muda
    }
}
```

## 3. Caso de uso

**Critério:** um caso de uso = **uma intenção do usuário** (ou de um sistema externo), uma classe,
um método `executar`, uma transação. Pergunta-teste: "o usuário faria isso sozinho, como um passo com sentido?"

| Situação | Quantos UCs |
|---|---|
| CRUD do rascunho | `Criar`, `Consultar`, `Listar`, `Alterar<Dados>`, `Excluir` |
| Seção editada como bloco (lista de itens) | **um** `Salvar<Secao>UC` com `PUT` da seção inteira — não um por item/entidade |
| Transições (submeter, enviar, cancelar...) | um UC cada **ou** Command (abaixo) |
| Fato de sistema externo | um `Receber<Fato>UC`, chamado pelo webhook |

O UC **orquestra, não decide**: carrega, chama o método de negócio, salva, devolve Output. Nenhum `if`
de regra no UC. Proibido UC com vários métodos públicos e UC por entidade-parte. `@Transactional` fica
**só no UC** (leitura também, para carregar coleções LAZY), nunca no Resource nem no repositório.

**Command para transições.** Quando várias transições têm o mesmo roteiro (travar → executar → salvar →
efeito externo), crie um **invocador único** e um comando por transição. O comando só orquestra; a regra
continua na entidade. Use quando houver 3+ transições parecidas; com 1–2, UCs simples bastam.

Ilustração: `application/pagamento/comando/ComandoPagamento` (sealed: `SubmeterPagamento`, `EnviarAoBanco`,
`ReenviarAoBanco`, `CancelarPagamento`), `ContextoComando`, invocador `ExecutarComandoPagamentoUC`:

```java
@Transactional
public PagamentoOutput executar(UUID id, ComandoPagamento comando) {
    var pagamento = repository.obterParaAlteracao(id);   // lock
    comando.executar(pagamento, contexto);
    repository.salvar(pagamento);
    aposCommit(comando, pagamento);   // registra comando.depoisDoCommit: EnviarAoBanco só chama o banco se confirmou
    return PagamentoOutput.de(pagamento);
}
```

O Resource monta o comando: `POST /{id}/cancelamento` → `executar.executar(id, new CancelarPagamento(motivo))`.

## 4. Regras de negócio: Specification + Chain of Responsibility

Base (templates): `Specification<T>` (`and/or/not`, `quando(...).entao(...)`), `RegraNegocio<T>`
(spec + exceção específica), `ValidadorRegras<T>` (`validar` fail-fast para comandos, `primeiraViolacao`
para fatos), `CadeiaValidacao<T>` (elos condicionais).

1. Cada regra é **pura**: recebe um **candidato** (record com tudo de que precisa, inclusive "hoje" e dados
   externos já buscados); não acessa banco, relógio nem rede. Regra simples = **uma linha no catálogo**
   (`RegraNegocio.de(condição, candidato -> new <Situacao>Exception(...))`); classe própria só se for
   parametrizada, tiver lógica não trivial ou mensagem com dados (estende `RegraNegocio<Candidato>`:
   `isSatisfiedBy` + `violacao`; ex.: `QuantidadeDeBoletosSpec(min, max)`).
2. As regras ficam num **catálogo** por agregado (`Regras<Agregado>`), agrupadas por **momento**:
   salvar seção, finalização, conformidade de fato externo. A entidade só chama o catálogo.
3. Regras que **variam por tipo/modalidade** viram elos de `CadeiaValidacao`: um elo comum + um elo por
   tipo, cada um com seu predicado de aplicação. Tipo novo = elo novo; nada de `if`/`switch` por tipo.

Ilustração: `domain/specification/pagamento/RegrasPagamento` — o catálogo é a "tabela de regras":

```java
private static final ValidadorRegras<SubmissaoPagamento> COMUNS = ValidadorRegras.<SubmissaoPagamento>de(
        de(s -> !s.boletos().vazio(), s -> new PagamentoSemBoletosException()),
        de(s -> s.dataPagamento() != null, s -> new DataPagamentoNaoInformadaException()),
        de(s -> !s.dataPagamento().isBefore(s.hoje()), s -> new DataPagamentoNoPassadoException(...)),  // fail-fast: data já existe
        new BoletosVencemAposPagamentoSpec());                                // lógica + mensagem: classe

public static final CadeiaValidacao<SubmissaoPagamento> SUBMISSAO = CadeiaValidacao
        .comecandoPor(COMUNS)                                                 // sempre
        .depois(s -> s.modalidade() == Modalidade.INDIVIDUAL, ValidadorRegras.de(new QuantidadeDeBoletosSpec(1, 1)))
        .depois(s -> s.modalidade() == Modalidade.PROCESSO, ValidadorRegras.<SubmissaoPagamento>de(
                new QuantidadeDeBoletosSpec(1, 20),
                de(s -> s.boletos().mesmoBeneficiario(), s -> new BeneficiariosDiferentesException())))
        .depois(s -> s.modalidade() == Modalidade.LOTE, ...);
```

## 5. Estados: State pattern em enum

- Um `enum Status<Agregado>` com **um método por ação**; por padrão **toda ação lança**
  `TransicaoEstadoInvalidaException` (409). Cada constante sobrescreve só o que permite e **devolve o próximo
  status**. A entidade persiste o enum (`@Enumerated(STRING)`) e faz `status = status.acao()`.
- A máquina inteira fica num arquivo. Classe por estado só se um estado tiver dados ou dependências próprios.
- Validações "pode editar/excluir?" também são do status (`validarEdicao`, `validarExclusao`).
- Proibido `if (status == ...)`/`switch (status)` fora do enum.

Ilustração: `domain/state/StatusPagamento`:

```java
public enum StatusPagamento {
    EM_EDICAO {
        @Override public StatusPagamento submeter() { return AGENDADO; }
        @Override public StatusPagamento cancelar() { return CANCELADO; }
        @Override public void validarEdicao() { }                 // só o rascunho edita
    },
    AGENDADO { @Override public StatusPagamento enviarAoBanco() { return EM_PROCESSAMENTO; } ... },
    PAGO, CANCELADO;                                              // finais: nada sobrescrito

    public StatusPagamento submeter() { throw acaoNaoPermitida("submeter"); }
    ...
}
```

## 6. Exceções

Quatro famílias, cada caso concreto é **uma classe própria** com código fixo (`CODIGO`):

| Família | HTTP | Quando |
|---|---|---|
| `DadoInvalidoException` | 400 | formato inválido (VO) |
| `RecursoNaoEncontradoException` | 404 | não existe |
| `ConflitoException` | 409 | conflita com o estado atual (State) ou recurso bloqueado (lock) |
| `RegraNegocioException` | 422 | regra violada (Specification) |

Um único `ApiExceptionHandler` mapeia **por família** (exceção nova não exige mexer nele) e também trata
Bean Validation/JSON inválido (400) e erro inesperado (500, sem vazar detalhes). Corpo: `ErroResponse(codigo, mensagem, campos)`.
Proibido `RuntimeException`/`IllegalStateException` genérica para regra de negócio.

## 7. Integração externa

- **Saída** (nós chamamos): classe em `adapter/<sistema>/` que traduz o contrato externo para VO/entidade
  do domínio e erros externos para exceções do domínio. O UC injeta direto.
  Ilustração: `adapter/emissor/EmissorBoletos.consultar(linha)` → `BoletoAPagar`; `adapter/banco/BancoPagamentos.enviar(pagamento)`.
- **Entrada** (eles nos chamam): entrypoint próprio (`entrypoint/webhook/v1`), com autenticação própria
  (token comparado em tempo constante), request no formato **deles** traduzido na borda para Input.
  Ilustração: `BancoWebhookResource` + `AutenticacaoWebhook` + `RetornoBancoRequest.paraInput()`.
- **Comando × fato.** Comando (usuário pede) pode ser **recusado** (422). Fato (algo já aconteceu lá fora)
  **não se recusa**: as mesmas specs **classificam** (`primeiraViolacao`) e o agregado vai para um estado
  de análise guardando o motivo. Ilustração: valor liquidado ≠ total → `EM_ANALISE` com `VALOR_LIQUIDADO_DIVERGENTE`.
- **Idempotência:** identifique o fato por um id que **nós** geramos e enviamos e que **carrega a chave do
  agregado** (VO com `de(String)`). Assim todo aviso — repetido ou de um envio antigo — chega ao agregado,
  que o ignora e responde 200; só agregado inexistente dá 404 (o sistema externo não reenvia para sempre).
  Ilustração: `IdTransacao(pagamento, envio)` → `"<uuid>-2"`; `ReceberRetornoBancoUC` faz `obterParaAlteracao(id.pagamento())`.
- **Efeito externo só depois do commit**, nunca dentro da transação: senão um commit que falha deixa o
  sistema externo com algo que não gravamos, e a resposta dele pode chegar antes de gravarmos. Registre um
  `Synchronization` no `TransactionSynchronizationRegistry` e aja em `afterCompletion(STATUS_COMMITTED)`.
  Ilustração: `ExecutarComandoPagamentoUC.aposCommit` + `ComandoPagamento.depoisDoCommit`. Falha depois do
  commit não desfaz nada: em produção, **Outbox** (gravar a intenção na mesma transação; um job envia e reenvia).

## 8. Repositório e concorrência (lock pessimista)

- `domain/repository/<Agregado>Repository implements PanacheRepositoryBase<<Agregado>, UUID>`, injetado direto
  no UC. Métodos são **intenções de negócio** que já tratam a consulta (filtros opcionais, ordenação, lock,
  "não encontrado"): `obter`, `obterParaAlteracao`, `listar(modalidade, status, pagina, tamanho)`, `remover`.
  Recebe parâmetros simples/VOs e devolve entidade ou `Pagina`; o UC não monta query nem filtro.
- `salvar` só faz `persist` da entidade nova (a carregada já é gerenciada).
- Quem vai **alterar** carrega com `SELECT ... FOR UPDATE`: `obterParaAlteracao(id)` com
  `LockModeType.PESSIMISTIC_WRITE` + hint `jakarta.persistence.lock.timeout`. Leitura pura usa `obter(id)`, sem lock.
- O repositório traduz `PessimisticLockException`/`LockTimeoutException`/`LockAcquisitionException` para
  uma `ConflitoException` específica (`RecursoBloqueadoException` → 409). Nada de exceção JPA sai dele.
- Coleções do agregado (`@ElementCollection`) ficam LAZY (`FOR UPDATE` não combina com join de coleção),
  com `@BatchSize(size = <página máxima>)` para a listagem não fazer N+1 (o padrão do Quarkus é 16).
- Sem `@Version` junto (um mecanismo só). O hint de espera varia por banco/dialeto (testado só no H2):
  confirme no banco real com o teste de lock.

Ilustração: `domain/repository/PagamentoRepository` (`listar` com filtros nulos = todos); `Pagamento.boletos`.

## 9. Testes

| Teste | O quê | Ilustração |
|---|---|---|
| Builder de cenários | dados **válidos** por padrão; cada teste muda só o que interessa | `domain/Cenarios` |
| Specs/catálogo | uma classe `@Nested` por elo/momento; o limite exato de cada regra | `RegrasPagamentoTest` |
| Estados | **tabela** ação → (origem → destino); o resto deve dar 409 | `StatusPagamentoTest` |
| Entidade | ordem 409 → 422 → muda; fatos e idempotência | `PagamentoTest` |
| Repositório e transação | 2ª transação no mesmo registro → 409; nº de consultas da listagem; efeito externo só com commit | `PagamentoRepositoryTest` |
| Ponta a ponta | fluxo HTTP feliz + um erro por família + webhook | `PagamentoResourceTest` |

Entidade, regras e estados: JUnit puro (anotação JPA não exige runtime). `@QuarkusTest`: repositório, adapter, entrypoint.

## 10. Iniciar um projeto no padrão

1. Descubra o **pacote base** (pergunte se não estiver claro) e o que já existe: mantenha a classe do
   projeto quando ela já cumpre o papel de um template.
2. Copie `templates/` desta pasta para `src/main/java/<pacote/base>/` (o que está em `templates/test/`
   vai para `src/test/java/<pacote/base>/`), removendo `.tmpl` e trocando `__PACOTE__` pelo pacote base.
   Contém: base de specs (`Specification`, `RegraNegocio`, `ValidadorRegras`, `CadeiaValidacao`), famílias de
   exceção, `Dinheiro`, `Motivo`, `Pagina`, `ClockProducer` (`app.fuso-horario`), `ApiExceptionHandler`,
   `ErroResponse`, `MotivoRequest`, `PaginaResponse`, `SpecificationTest`. Sem template (criadas com o agregado):
   `TransicaoEstadoInvalidaException` (recebe o `Status<Agregado>`), `<Agregado>NaoEncontradoException`, `RecursoBloqueadoException`.
3. Dependências Quarkus: `quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm-panache`
   (+ driver; os templates `Dinheiro`/`Motivo` já trazem o `Conversor` JPA); testes com `quarkus-junit`
   (`quarkus-junit5` em versões antigas) e `rest-assured`. Em Spring, adapte entrypoint/config
   (`@RestControllerAdvice`, `@Bean`, `@Value`) e troque o Panache por Spring Data.
4. Copie `mapa-do-projeto.md` para o fim de `.github/copilot-instructions.md` e preencha com os nomes reais.
5. `mvn test` (o `SpecificationTest` deve passar) e `verificar-camadas.sh`.

**Novo agregado CRUD**, na ordem: VOs → exceções → catálogo de regras → enum de status → entidade (JPA) →
testes de domínio → repositório → Input/Output + UCs → Request/Response + Resource → teste ponta a ponta.

## 11. Revisão (checklist)

Rode `bash .github/skills/layered-crud/verificar-camadas.sh` (dependências entre camadas, setters,
construtor público, `now()` sem clock, `if` em status, UC chamando UC, pastas fora da estrutura). Depois confira:

- [ ] Entidade JPA sem setters, construtor sem argumentos `protected`, sem `PanacheEntity`; ordem estado →
      regras → muda; nenhum `if` de regra no UC.
- [ ] Regra pura com exceção específica, no catálogo (linha `de(...)` ou classe só se justificar); variação por tipo na cadeia.
- [ ] Status só via enum com comportamento; tabela de estados testada.
- [ ] Um UC por intenção (ou Command com invocador único); seção = um `Salvar<Secao>UC`.
- [ ] Alteração usa `obterParaAlteracao` (lock); leitura usa `obter`; coleção LAZY com `@BatchSize`.
- [ ] Efeito em sistema externo só depois do commit (Synchronization/Outbox), nunca dentro da transação.
- [ ] Fato externo classificado (não recusado), idempotente, entrando por entrypoint próprio autenticado.
- [ ] DTO/mapper só entre entrypoint e application; nada sem ganho real (filtro, id embrulhado, mapper interno);
      repositório com métodos de intenção que já tratam filtros.
- [ ] Testes: cenário válido por padrão, limites das regras, tabela de estados, um erro por família na API.
