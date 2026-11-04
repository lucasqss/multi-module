# Instruções do repositório — pagamento-boleto

Este projeto é um **CRUD de referência** (Quarkus 3, Java 25, Maven, projeto único em `src/`).
Ele serve de modelo para outros projetos, então **consistência com os padrões existentes vale mais
que a solução mais curta**. Na dúvida, copie a forma de uma classe existente equivalente.

Domínio: **processo de pagamento de boletos**. O boleto pertence a um sistema externo (o **emissor**);
o que é nosso é o processo: escolher os boletos, aprovar (com alçada), enviar ao **banco** (outro
sistema externo) e acompanhar o retorno. Código, nomes e mensagens em **português**; termos técnicos consagrados
(Repository, Factory, Spec, UC) ficam em inglês.

## Arquitetura em camadas (regra de dependência)

```
entrypoint ──▶ application ──▶ domain
                    │             ▲
                    └─▶ adapter ──┘
```

Sem ports and adapters: as camadas se ligam por **injeção de dependência de classes concretas**.
O use case injeta o repositório/integração de `adapter` diretamente; não crie interfaces "de porta".

Pacote base `com.example.pagamento`:

| Camada | Pode importar | NUNCA importa |
|---|---|---|
| `domain` | só `java.*` e `shared` | `jakarta.*`, `io.quarkus.*`, `org.hibernate.*`, `application`, `adapter`, `entrypoint` |
| `application` | `domain`, `adapter`, `shared`, `jakarta.transaction`, `jakarta.enterprise` | `entrypoint`, JPA (`jakarta.persistence`) |
| `adapter` | `domain`, `shared`, frameworks | `application`, `entrypoint` |
| `entrypoint` | `application`, `domain` (só enums de status/valor e exceções), `shared` | `adapter` |

Verifique com `bash .github/skills/layered-revisao-arquitetura/verificar-camadas.sh`.

## Estrutura de pastas (por tipo, enxuta)

```
com.example.pagamento
├── domain
│   ├── entity          entidades (aggregate roots)
│   ├── valueobject     value objects (records) e enums de valor
│   ├── exception       famílias + exceções específicas
│   ├── specification   base genérica (Specification, RegraNegocio, ValidadorRegras)
│   │   └── <agregado>  specs, catálogo Regras<Agregado> e candidatos (Solicitacao<Agregado>...)
│   └── state           Estado<Agregado>, estados concretos e enum Status<Agregado>
├── application
│   └── <agregado>      *UC (+ dto/ com *Input/*Output)
├── adapter
│   ├── database        <Agregado>Repository, *JpaEntity, *Mapper, Filtro*
│   └── <sistema>       uma pasta por sistema externo
├── entrypoint
│   ├── api/v1          *Resource, request/, response/, handler/
│   └── webhook/v1      entrada de sistemas externos (outros: iib/v1, consumidores de fila...)
└── shared              Pagina, ClockProducer (sem subpastas)
```

**Não crie** estas pastas (cada uma tem um destino melhor):

| Pasta | Para onde vai o conteúdo |
|---|---|
| `domain/factory` | Criação = método estático da entidade (`ProcessoPagamento.criar`) |
| `domain/model` | Candidato das regras → `specification/<agregado>`; DTO → `application/.../dto` ou `adapter/<sistema>` |
| `domain/enums` | Status → `state`; enum de valor → `valueobject`; enum usado por um só VO → método do VO |
| `domain/repository`, `domain/gateway` | Repositório/integração concretos em `adapter/` (sem ports) |
| `domain/service` | Lógica de uma entidade → na entidade; regra → spec; orquestração → use case |
| `shared/utils`, `shared/config` | `shared/` direto; função de um conceito → método do VO |

## Interação entre classes (quem chama quem)

```
Resource/Consumer ─▶ UC ─┬─▶ Repository / integração (adapter) ─▶ Mapper ─▶ Entidade.reconstituir
                         ├─▶ Solicitacao<Agregado>.para<Momento>(...)   (monta o candidato)
                         └─▶ Entidade.<metodoDeNegocio>(...) ─┬─▶ Estado (autoriza a ação)
                                                              └─▶ Regras<Agregado> ─▶ *Spec ─▶ candidato
```

- Cada classe conversa só com a camada **imediatamente** abaixo dela nesse fluxo.
- Resource não toca repositório nem entidade; **UC não chama outro UC**, nem spec, nem estado.
- Entidade nunca chama repositório; spec só lê o candidato; estado só conhece estados.
- Lógica reaproveitada entre UCs mora na entidade, num VO ou numa spec, nunca num UC compartilhado
  nem numa classe `*Service`/`*Utils` genérica.

## Regras inegociáveis

1. **Domínio rico, nunca anêmico.** Entidades não têm setters públicos nem construtor público.
   Estado muda só por métodos com nome de negócio (`emitir`, `cancelar`, `receberPagamento`).
   Criação por fábrica estática (`ProcessoPagamento.criar`); reconstrução do banco por `reconstituir`.
2. **Use case orquestra, não decide.** Um `*UC` por intenção do usuário (ver "O que é um caso de uso"): busca fatos nos repositórios/integrações, delega a decisão
   ao domínio, persiste, devolve DTO. **Nenhum `if` de regra de negócio em use case.**
3. **Regras de negócio = Specification pattern.** Cada regra é uma `RegraNegocio<T>` (ou composição
   de `Specification<T>`) registrada no catálogo `RegrasProcessoPagamento`. Specs são **puras**: fatos que exigem
   I/O são consultados pelo use case e entregues no candidato (ex.: "hoje" em `SubmissaoProcesso`, dados do emissor em `BoletoAPagar`).
4. **Máquina de estados = State pattern.** Uma classe por estado em `domain.state`, `sealed`,
   que **proíbe tudo por padrão**. Transições retornam o próximo estado. Nunca use `if (status == X)`.
5. **Exceções específicas.** Uma classe por situação, estendendo uma das famílias:
   `DadoInvalidoException` (400), `RecursoNaoEncontradoException` (404), `ConflitoException` (409),
   `RegraNegocioException` (422). Cada uma com `CODIGO` fixo e dados no construtor.
   Erro de programação usa `IllegalArgumentException`/`IllegalStateException`, não `DomainException`.
6. **Value objects auto-validados** (`record` com construtor compacto) para todo conceito com regra de
   formato: dinheiro, documento, e-mail, ids. Nada de `BigDecimal`/`String` soltos no domínio.
7. **Adapter fala a língua do domínio:** repositórios e integrações recebem/devolvem entidades e VOs de
   domínio, nunca `*JpaEntity` ou DTO externo, e traduzem exceções técnicas para exceções de domínio.
   Interface só quando houver **mais de uma implementação real** (não "para o futuro").
8. **Contratos separados por camada:** `*Request`/`*Response` (entrypoint), `*Input`/`*Output`
   (application), entidade de domínio, `*JpaEntity` (adapter). A entidade de domínio nunca sai da application.
9. **Tempo injetado.** Nunca `LocalDate.now()`/`Instant.now()` sem `Clock`. O `Clock` usa o fuso do negócio.
10. **Ordem dentro da entidade:** 1º o estado valida a ação (409) → 2º as specs validam os dados (422) → 3º muda o estado.

## O que é um caso de uso (critério)

**Um caso de uso = uma intenção do usuário (ou de outro sistema) com significado de negócio**,
executada numa transação. Teste rápido: dá para dizer "o <ator> quer <verbo> <algo>" e a tela ou o
sistema dispara isso como **uma** ação? Então é um caso de uso.

- **Uma classe, um `executar`, uma transação.** Nunca um UC com vários métodos públicos: isso é um
  `*Service` disfarçado (vários motivos para mudar, dependências acumuladas, transações misturadas).
- **Não é um por entidade.** Partes de um agregado (itens, destinatários, parcelas, documentos) não têm
  UC nem repositório próprios: o UC carrega o agregado, chama o método dele e salva o agregado inteiro.
- **Não é um por operação técnica** (incluir/alterar/remover item). Seção que a tela edita como um
  bloco → **um UC por seção**, com `PUT` substituindo a seção inteira.
- **Separe** em UCs diferentes quando muda: a regra, a permissão, a transição de estado ou o ator/gatilho
  (usuário × banco × job). **Junte** quando sempre acontecem juntos, com a mesma regra e o mesmo gatilho.

| Situação | Quantos casos de uso | Exemplo |
|---|---|---|
| Criar o agregado | 1 — nasce em `EM_EDICAO` se pode ficar incompleto | `CriarProcessoPagamentoUC` |
| Editar uma seção/parte editada como bloco | 1 por seção (`PUT` substitui a seção) | `SalvarBoletosDoProcessoUC`, `AlterarProcessoPagamentoUC` (conta e data) |
| Incluir/remover uma parte que é ação de negócio própria (regras, permissão ou evento próprios) | 1 por ação | (não há no exemplo; ex.: `AnexarComprovanteUC`) |
| Finalizar a edição (exige completude) | 1 | `SubmeterProcessoPagamentoUC` |
| Aviso de **sistema externo** (fato: pagamento, retorno de processamento) | 1, chamado por um entrypoint de integração (webhook/fila) | `ReceberRetornoBancoUC`, `ReceberCancelamentoBoletoUC` (webhooks) |
| Transição de estado / evento de negócio | 1 por transição | `AprovarProcessoPagamentoUC`, `RejeitarProcessoPagamentoUC`, `EnviarProcessoParaPagamentoUC`, `RetentarPagamentoUC`, `CancelarProcessoPagamentoUC` |
| Consultar | 1 por consulta | `ConsultarProcessoPagamentoUC`, `ListarProcessosPagamentoUC` |
| Entidade com vida própria, usada por vários agregados | CRUD próprio; aqui só o id | o **boleto**: é do emissor; aqui só a cópia `BoletoAPagar` e o id dele |

### Comando × fato

- **Comando** = alguém pede algo ("emita", "cancele"). Pode ser recusado: regra violada → 422.
- **Fato** = algo que já aconteceu lá fora ("o pagador pagou R$ 499"). **Não** se recusa: o domínio
  registra e **classifica**. As mesmas regras servem: `ValidadorRegras.primeiraViolacao(...)` não lança,
  devolve a regra não atendida, que vira o motivo de um estado de análise.
- Fatos vindos de fora chegam repetidos (webhook/fila entregam "pelo menos uma vez"): o agregado ignora
  o mesmo id externo (**idempotência**).

### Agregado editado por partes (padrão `EM_EDICAO`)

1. `CriarProcessoPagamentoUC` cria o rascunho em `EM_EDICAO` só com o mínimo.
2. Um caso de uso por seção (`SalvarBoletosDoProcessoUC`, `AlterarProcessoPagamentoUC`); cada um valida **só a seção** (regras de seção no catálogo).
   O agregado pode continuar incompleto.
3. `SubmeterProcessoPagamentoUC` exige a **completude** (conjunto de regras de finalização no catálogo, sobre o agregado
   inteiro) e muda o estado. Só a partir daí o agregado "vale" (é cobrável, visível, enviado...).
4. O estado `EM_EDICAO` permite editar/excluir; os seguintes não (`validarEdicao()` → 409).
5. `@Version` no agregado: duas edições concorrentes, mesmo de seções diferentes, não se sobrescrevem.

## Comandos

```bash
mvn test            # todos os testes (domínio em ms; @QuarkusTest sobe a app com H2)
mvn quarkus:dev     # http://localhost:8081
```

Toda mudança de comportamento vem com teste. Testes de domínio são JUnit puro (sem Quarkus) e usam
o builder `Cenarios`. Só testes de API/adapter usam `@QuarkusTest`.

## Skills disponíveis (`.github/skills/`)

As skills são **genéricas** (valem para qualquer projeto no padrão) e usam este repositório só como
**ilustração do "to be"**. Elas encontram as classes deste projeto pelo "Mapa do projeto" abaixo.

- `layered-novo-caso-de-uso` — decidir se é um caso de uso e criar a ação de ponta a ponta
- `layered-nova-regra-de-negocio` — validação de negócio (spec + exceção + catálogo + teste)
- `layered-novo-estado` — estado/transição (State pattern)
- `layered-novo-agregado-crud` — novo agregado com CRUD completo
- `layered-nova-integracao-externa` — integração de saída (adapter) e de entrada (webhook/fila)
- `layered-revisao-arquitetura` — revisão de camadas, domínio anêmico, SOLID/GRASP (+ `verificar-camadas.sh`)
- `layered-enxugar-estrutura` — eliminar pastas desnecessárias e corrigir quem chama quem
- `layered-iniciar-projeto` — criar a base genérica e o "Mapa do projeto" num projeto novo

## Mapa do projeto

<!-- Lido pelas skills genéricas layered-* (.github/skills). Mantenha os nomes em dia. -->

| Papel | Neste projeto |
|---|---|
| Pacote base | `com.example.pagamento` (`src/main/java/com/example/pagamento`) |
| Entrypoints | `entrypoint/api/v1` (nossa API), `entrypoint/webhook/v1` (`BancoWebhookResource`: retorno do banco; `EmissorWebhookResource`: boleto cancelado) |
| Agregados | `ProcessoPagamento` (`domain/entity/ProcessoPagamento`) |
| Entidades internas do agregado | `Aprovacao`, `TentativaPagamento` (sem repositório nem UC próprios) |
| Partes substituídas inteiras | `BoletosDoProcesso` (cópias `BoletoAPagar` do emissor), `ContaDebito` |
| Base de specs | `domain/specification/Specification`, `RegraNegocio`, `ValidadorRegras` |
| Catálogo de regras | `RegrasProcessoPagamento` — por momento: seção `BOLETOS`; finalização `SUBMISSAO`; `APROVACAO` + `ALCADA_ATINGIDA` (decisão); `CONFORMIDADE_RETORNO` (classifica fato) |
| Candidatos das regras | `domain/specification/processo/`: `SubmissaoProcesso`, `AprovacaoProcesso`, `RetornoPagamento` — montados pela raiz |
| Agregado editado por partes | `ProcessoPagamento`: `EM_EDICAO` → seções (`SalvarBoletosDoProcessoUC`, `AlterarProcessoPagamentoUC`) → `SubmeterProcessoPagamentoUC` |
| Máquina de estados | `domain/state`: `EstadoProcesso`, estados concretos e enum `StatusProcesso` |
| Repositórios (injetados nos UCs) | `adapter/database/ProcessoPagamentoRepository` (Panache, fala domínio) |
| Integrações externas (injetadas nos UCs) | `adapter/emissor/EmissorBoletos` (consulta boleto), `adapter/banco/BancoPagamentos` (envia pagamento) |
| Handler de erros HTTP | `entrypoint/api/v1/handler/ApiExceptionHandler` |
| `Clock` no fuso do negócio | `shared/ClockProducer` |
| Builder de cenários de teste | `src/test/java/com/example/pagamento/domain/Cenarios` |
| Teste de tabela de estados | `EstadoProcessoTest` (mapa `PERMITIDAS`) |
| Comandos | `mvn test`, `mvn quarkus:dev` |
