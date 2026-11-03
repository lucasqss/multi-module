# Instruções do repositório — pagamento-boleto

Este projeto é um **CRUD de referência** (Quarkus 3, Java 25, Maven, projeto único em `src/`).
Ele serve de modelo para outros projetos, então **consistência com os padrões existentes vale mais
que a solução mais curta**. Na dúvida, copie a forma de uma classe existente equivalente.

Domínio: ciclo de vida de um boleto — emissão (gera linha digitável), notificação ao pagador e
recebimento do pagamento. Código, nomes e mensagens em **português**; termos técnicos consagrados
(Repository, Factory, Spec, UC) ficam em inglês.

## Arquitetura em camadas (regra de dependência)

```
entrypoint ──▶ application ──▶ domain ◀── adapter
```

Pacote base `com.example.boleto`:

| Camada | Pode importar | NUNCA importa |
|---|---|---|
| `domain` | só `java.*` e `shared` | `jakarta.*`, `io.quarkus.*`, `org.hibernate.*`, `application`, `adapter`, `entrypoint` |
| `application` | `domain`, `shared`, `jakarta.transaction`, `jakarta.enterprise` | `adapter`, `entrypoint`, JPA |
| `adapter` | `domain`, `shared`, frameworks | `application`, `entrypoint` |
| `entrypoint` | `application`, `domain` (só enums/exceções), `shared` | `adapter` |

Verifique com `./.github/skills/revisao-arquitetura/verificar-camadas.sh`.

## Regras inegociáveis

1. **Domínio rico, nunca anêmico.** Entidades não têm setters públicos nem construtor público.
   Estado muda só por métodos com nome de negócio (`emitir`, `cancelar`, `receberPagamento`).
   Criação por fábrica estática que valida (`Boleto.emitir`); reconstrução do banco por `reconstituir`.
2. **Use case orquestra, não decide.** Um `*UC` por ação: busca fatos nas portas, delega a decisão
   ao domínio, persiste, devolve DTO. **Nenhum `if` de regra de negócio em use case.**
3. **Regras de negócio = Specification pattern.** Cada regra é uma `RegraNegocio<T>` (ou composição
   de `Specification<T>`) registrada no catálogo `RegrasBoleto`. Specs são **puras**: fatos que exigem
   I/O são consultados pelo use case e entregues no candidato (`SolicitacaoBoleto`, `RecebimentoPagamento`).
4. **Máquina de estados = State pattern.** Uma classe por estado em `domain.state`, `sealed`,
   que **proíbe tudo por padrão**. Transições retornam o próximo estado. Nunca use `if (status == X)`.
5. **Exceções específicas.** Uma classe por situação, estendendo uma das famílias:
   `DadoInvalidoException` (400), `RecursoNaoEncontradoException` (404), `ConflitoException` (409),
   `RegraNegocioException` (422). Cada uma com `CODIGO` fixo e dados no construtor.
   Erro de programação usa `IllegalArgumentException`/`IllegalStateException`, não `DomainException`.
6. **Value objects auto-validados** (`record` com construtor compacto) para todo conceito com regra de
   formato: dinheiro, documento, e-mail, ids. Nada de `BigDecimal`/`String` soltos no domínio.
7. **Contratos separados por camada:** `*Request`/`*Response` (entrypoint), `*Input`/`*Output`
   (application), entidade de domínio, `*JpaEntity` (adapter). A entidade de domínio nunca sai da application.
8. **Tempo injetado.** Nunca `LocalDate.now()`/`Instant.now()` sem `Clock`. O `Clock` usa o fuso do negócio.
9. **Ordem dentro da entidade:** 1º o estado valida a ação (409) → 2º as specs validam os dados (422) → 3º muda o estado.

## Comandos

```bash
mvn test            # todos os testes (domínio em ms; @QuarkusTest sobe a app com H2)
mvn quarkus:dev     # http://localhost:8081
```

Toda mudança de comportamento vem com teste. Testes de domínio são JUnit puro (sem Quarkus) e usam
o builder `Cenarios`. Só testes de API/adapter usam `@QuarkusTest`.

## Skills disponíveis (`.github/skills/`)

- `nova-regra-de-negocio` — adicionar validação de negócio (spec + exceção + catálogo + teste)
- `novo-estado` — adicionar/alterar estado ou transição
- `novo-caso-de-uso` — nova ação de ponta a ponta (UC + endpoint + DTOs + testes)
- `novo-agregado-crud` — criar outro CRUD completo seguindo este modelo
- `nova-integracao-externa` — nova porta (`domain.gateway`) + adapter
- `revisao-arquitetura` — checklist de SOLID/GRASP/camadas/domínio anêmico para revisar mudanças
