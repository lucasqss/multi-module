---
name: layered-crud
description: Padrão para projetos Java (Quarkus) em camadas (domain, application, adapter, entrypoint, shared) com domínio rico, ligados por injeção de dependência direta (sem ports and adapters). Cobre entidade/VO, caso de uso (e Command para transições), Specification + Chain of Responsibility, State, exceções por família, integração externa (saída, webhook, comando × fato), lock pessimista, testes e revisão. Use ao criar ou alterar agregado, caso de uso, regra, estado, integração ou endpoint, ao iniciar um projeto no padrão e ao revisar código.
---

# CRUD layered com domínio rico

> **Sobre os exemplos.** As regras abaixo são genéricas. Os trechos **Ilustração** vêm do repositório de
> referência **pagamento-boleto** (`lucasqss/multi-module`) e só mostram o "to be": em outro projeto esses
> arquivos não existem. Use os nomes do **Mapa do projeto** (fim de `.github/copilot-instructions.md`);
> se ele não existir, siga a seção 10 antes de tudo.

## 1. Camadas e estrutura

```
<pacote.base>/
├── domain/        entity, valueobject, exception, specification[/<agregado>], state   (só java.*)
├── application/   <agregado>/  *UC  +  dto/  (+ comando/ quando houver Command)
├── adapter/       database/ (Repository + JpaEntity + Mapper)   <sistema-externo>/
├── entrypoint/    api/v1/ (Resource, request/, response/, handler/)   webhook/v1/ (ou iib/v1)
└── shared/        utilitários técnicos SEM subpastas (Pagina, ClockProducer)
```

Dependências permitidas: `entrypoint → application → (domain, adapter)`; `adapter → domain`; `domain → nada`.

- **Sem ports and adapters.** O UC injeta a classe concreta de `adapter/` (repositório, cliente externo),
  que já fala a língua do domínio (recebe/devolve entidade e VO, nunca JpaEntity/DTO externo).
  Interface só quando existir mais de uma implementação **real**.
- **Quem chama quem:** Resource → UC → (repositório/integração, entidade) ; entidade → estado e regras.
  UC não chama UC; não existe `*Service`/`*Helper`/`*Utils` genérico.
- **Estrutura enxuta:** nada de `domain/{factory,model,enums,repository,service}`, nem subpastas em
  `shared/`. Ao encontrar uma, **mova e apague a pasta** (enum de status → `state/`; outros enums e
  records → `valueobject/`; fábrica → método estático `criar` na entidade; utilitário de regra → spec/VO).
- **Contratos separados:** Request/Response (HTTP, versionado) ≠ Input/Output (application) ≠ entidade ≠ JpaEntity.

## 2. Entidade rica e value objects

- Construtor **privado**; `criar(...)` (aplica regras do nascimento) e `reconstituir(...)` (vem do banco,
  sem regras). **Sem setters**: só métodos de negócio com nome do domínio.
- Ordem dentro de todo método que muda estado: **1º estado autoriza (409) → 2º regras validam (422)
  → 3º muda**. Se algo lança, nada mudou.
- Conceito com regra de formato vira **VO** (record imutável que se valida no construtor e lança
  `DadoInvalidoException` específica → 400). Tempo sempre via `Clock` injetado (nunca `now()` sem clock).
- Agregado editado por partes nasce `EM_EDICAO` e pode ficar incompleto; **só a finalização exige completude**.

Ilustração: `domain/entity/Pagamento` — `criar(modalidade, agora)`, `salvarBoletos`, `alterarDadosGerais`,
`submeter(hoje)`, `registrarRetornoBanco(retorno)`.

```java
public void submeter(LocalDate hoje) {
    var proximo = estado.submeter();                                   // 409 se não pode
    RegrasPagamento.SUBMISSAO.validar(new SubmissaoPagamento(...));    // 422 se viola
    estado = proximo;                                                  // só então muda
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
de regra no UC. Proibido UC com vários métodos públicos e UC por entidade-parte.

**Command para transições.** Quando várias transições têm o mesmo roteiro (travar → executar → salvar →
efeito externo), crie um **invocador único** e um comando por transição. O comando só orquestra; a regra
continua na entidade. Use quando houver 3+ transições parecidas; com 1–2, UCs simples bastam.

Ilustração: `application/pagamento/comando/ComandoPagamento` (sealed: `SubmeterPagamento`, `EnviarAoBanco`,
`ReenviarAoBanco`, `CancelarPagamento`), `ContextoComando`, invocador `ExecutarComandoPagamentoUC`:

```java
@Transactional
public PagamentoOutput executar(UUID id, ComandoPagamento comando) {
    var pagamento = repository.obterParaAlteracao(new PagamentoId(id));   // lock
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

1. Uma spec por regra, **pura**: recebe um **candidato** (record com tudo de que precisa, inclusive
   "hoje" e dados externos já buscados); não acessa banco, relógio nem rede.
2. A spec sabe a própria violação (`violacao(candidato)` → exceção `RegraNegocioException` específica).
3. As regras ficam num **catálogo** por agregado (`Regras<Agregado>`), agrupadas por **momento**:
   salvar seção, finalização, conformidade de fato externo. A entidade só chama o catálogo.
4. Regras que **variam por tipo/modalidade** viram elos de `CadeiaValidacao`: um elo comum + um elo por
   tipo, cada um com seu predicado de aplicação. Tipo novo = elo novo; nada de `if`/`switch` por tipo.

Ilustração: `domain/specification/pagamento/RegrasPagamento`:

```java
public static final CadeiaValidacao<SubmissaoPagamento> SUBMISSAO = CadeiaValidacao
        .comecandoPor(COMUNS)                                                    // sempre
        .depois(s -> s.modalidade() == Modalidade.INDIVIDUAL, ValidadorRegras.de(
                new QuantidadeDeBoletosSpec(1, 1)))
        .depois(s -> s.modalidade() == Modalidade.PROCESSO, ValidadorRegras.de(
                new QuantidadeDeBoletosSpec(1, 20), new MesmoBeneficiarioSpec()))
        .depois(s -> s.modalidade() == Modalidade.LOTE, ValidadorRegras.de(
                new QuantidadeDeBoletosSpec(2, 100), TOTAL_DENTRO_DO_LIMITE_DO_LOTE));
```

## 5. Estados: State pattern

- Enum `Status<Agregado>` (persistido) + classe abstrata **sealed** `Estado<Agregado>` com um método por
  ação; por padrão **toda ação lança** `TransicaoEstadoInvalidaException` (409).
- Cada estado concreto (singleton `INSTANCIA`) sobrescreve só o que permite e **retorna o próximo estado**.
  `de(status)` é um `switch` exaustivo (sealed garante que nenhum fica de fora).
- Validações "pode editar/excluir?" também são do estado (`validarEdicao`, `validarExclusao`).
- Proibido `if (status == ...)`/`switch (status)` fora de `de(status)`.

Ilustração: `domain/state/EstadoPagamento` e `EstadoEmEdicao`:

```java
@Override public EstadoPagamento submeter() { return EstadoAgendado.INSTANCIA; }
@Override public EstadoPagamento cancelar() { return EstadoCancelado.INSTANCIA; }
@Override public void validarEdicao() { }   // só o rascunho edita
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

## 8. Concorrência: lock pessimista

- Quem vai **alterar** carrega com `SELECT ... FOR UPDATE`: `repository.obterParaAlteracao(id)` com
  `LockModeType.PESSIMISTIC_WRITE` + hint `jakarta.persistence.lock.timeout`. Leitura pura usa `obter(id)`, sem lock.
- O repositório traduz `PessimisticLockException`/`LockTimeoutException`/`LockAcquisitionException` para
  uma `ConflitoException` específica (`RecursoBloqueadoException` → 409). Nada de JPA vaza do adapter.
- Coleções do agregado (`@ElementCollection`) ficam LAZY (`FOR UPDATE` não combina com join de coleção),
  com `@BatchSize(size = <página máxima>)` para a listagem não fazer N+1 (o padrão do Quarkus é 16).
- Sem `@Version` junto: um mecanismo só.

Ilustração: `adapter/database/PagamentoRepository.obterParaAlteracao`; `PagamentoJpaEntity.boletos`.

## 9. Testes

| Teste | O quê | Ilustração |
|---|---|---|
| Builder de cenários | dados **válidos** por padrão; cada teste muda só o que interessa | `domain/Cenarios` |
| Specs/catálogo | uma classe `@Nested` por elo/momento; o limite exato de cada regra | `RegrasPagamentoTest` |
| Estados | **tabela** ação → (origem → destino); o resto deve dar 409 | `EstadoPagamentoTest` |
| Entidade | ordem 409 → 422 → muda; fatos e idempotência | `PagamentoTest` |
| Repositório e transação | 2ª transação no mesmo registro → 409; nº de consultas da listagem; efeito externo só com commit | `PagamentoRepositoryTest` |
| Ponta a ponta | fluxo HTTP feliz + um erro por família + webhook | `PagamentoResourceTest` |

Domínio testa sem Quarkus (JUnit puro, rápido). `@QuarkusTest` só para adapter e entrypoint.

## 10. Iniciar um projeto no padrão

1. Descubra o **pacote base** (pergunte se não estiver claro) e o que já existe: mantenha a classe do
   projeto quando ela já cumpre o papel de um template.
2. Copie `templates/` desta pasta para `src/main/java/<pacote/base>/` (o que está em `templates/test/`
   vai para `src/test/java/<pacote/base>/`), removendo `.tmpl` e trocando `__PACOTE__` pelo pacote base.
   Contém: base de specs (`Specification`, `RegraNegocio`, `ValidadorRegras`, `CadeiaValidacao`), famílias de
   exceção, `Dinheiro`, `Motivo`, `Pagina`, `ClockProducer` (`app.fuso-horario`), `ApiExceptionHandler`,
   `ErroResponse`, `MotivoRequest`, `PaginaResponse` e `SpecificationTest`.
3. Dependências Quarkus: `quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm-panache`
   (+ driver) quando houver banco; testes com `quarkus-junit5` e `rest-assured`. Em Spring, adapte só
   entrypoint/config (`@RestControllerAdvice`, `@Bean`, `@Value`).
4. Copie `mapa-do-projeto.md` para o fim de `.github/copilot-instructions.md` e preencha com os nomes reais.
5. `mvn test` (o `SpecificationTest` deve passar) e `verificar-camadas.sh`.

**Novo agregado CRUD**, na ordem: VOs → exceções → specs + catálogo → estados → entidade → testes de
domínio → JpaEntity + Mapper + Repository → Input/Output + UCs → Request/Response + Resource → teste ponta a ponta.

## 11. Revisão (checklist)

Rode `bash .github/skills/layered-crud/verificar-camadas.sh` (dependências entre camadas, setters,
construtor público, `now()` sem clock, `if` em status, UC chamando UC, pastas fora da estrutura). Depois confira:

- [ ] Entidade sem setters; ordem estado → regras → muda; nenhum `if` de regra no UC.
- [ ] Cada regra é uma spec pura com exceção específica, registrada no catálogo; variação por tipo na cadeia.
- [ ] Status só via State; tabela de estados testada.
- [ ] Um UC por intenção (ou Command com invocador único); seção = um `Salvar<Secao>UC`.
- [ ] Alteração usa `obterParaAlteracao` (lock); leitura usa `obter`.
- [ ] Fato externo classificado (não recusado), idempotente, entrando por entrypoint próprio autenticado.
- [ ] Request/Response separados de Input/Output; nenhum JpaEntity fora de `adapter/database`.
- [ ] Testes: cenário válido por padrão, limites das regras, tabela de estados, um erro por família na API.
