---
name: novo-agregado-crud
description: Cria um novo agregado com CRUD completo (entidade rica, value objects, specifications, State, exceções específicas, portas, adapter JPA, use cases, API REST e testes) copiando a estrutura do agregado Boleto. Use quando pedirem um novo cadastro/recurso/entidade, ou para replicar este modelo em outro projeto.
---

# Novo agregado com CRUD completo

O agregado `Boleto` é o **gabarito**. Para um agregado `<Agregado>` (ex.: `Convenio`), crie os
arquivos equivalentes abaixo, **lendo o arquivo-modelo antes de escrever cada um**. Não invente
estrutura nova: a consistência entre agregados é o objetivo.

Antes de codificar, escreva (e confirme com o usuário, se não estiver claro):
1. **Estados e transições** (diagrama como o de `StatusBoleto`). Sem ciclo de vida? Pule o State.
2. **Regras de negócio** e em que momento valem (criação, alteração, outra ação).
3. **Dados** e quais têm regra de formato (viram value objects).
4. **Ações** além do CRUD (viram `POST /{id}/<substantivo>`).

## Arquivos (de dentro para fora)

Base: `src/main/java/com/example/boleto/`. Coloque cada agregado no seu subpacote quando houver
mais de um (`domain/entity`, `application/<agregado>`, `adapter/database`...).

| # | Criar | Modelo |
|---|---|---|
| 1 | `domain/valueobject/<Agregado>Id`, demais VOs | `BoletoId`, `Documento`, `Pagador` |
| 2 | `domain/enums/Status<Agregado>` (só o rótulo) | `StatusBoleto` |
| 3 | `domain/state/Estado<Agregado>` (`sealed`) + um `final` por estado | `EstadoBoleto`, `EstadoEmitido` |
| 4 | `domain/exception/<Situacao>Exception` (uma por caso, na família certa) | `BoletoNaoEncontradoException`, `BoletoVencidoException` |
| 5 | `domain/model/Solicitacao<Agregado>` (candidato das regras) | `SolicitacaoBoleto` |
| 6 | `domain/specification/<agregado>/...Spec` + catálogo `Regras<Agregado>` | `specification/boleto/*`, `RegrasBoleto` |
| 7 | `domain/entity/<Agregado>` (rica: fábrica estática, `reconstituir`, sem setters) | `Boleto` |
| 8 | `domain/factory/Solicitacao<Agregado>Factory` + registro em `shared/config/DomainBeansProducer` | `SolicitacaoBoletoFactory` |
| 9 | `domain/repository/<Agregado>Repository` (+ `Filtro<Agregado>`), com `default obter(id)` | `BoletoRepository` |
| 10 | `adapter/database/<Agregado>JpaEntity` (`@Version`), `<Agregado>Mapper`, `<Agregado>RepositoryPanache` | `BoletoJpaEntity`, `BoletoMapper`, `BoletoRepositoryPanache` |
| 11 | `application/<agregado>/dto/*Input`, `<Agregado>Output` | `EmitirBoletoInput`, `BoletoOutput` |
| 12 | `application/<agregado>/<Verbo><Agregado>UC` (um por ação) | `EmitirBoletoUC`, `CancelarBoletoUC` |
| 13 | `entrypoint/api/v1/request/*Request`, `response/<Agregado>Response`, `<Agregado>Resource` | `EmitirBoletoRequest`, `BoletoResponse`, `BoletoResource` |

**Reaproveite, não duplique:** `Specification`, `RegraNegocio`, `ValidadorRegras`, famílias de
exceção, `Dinheiro`, `Motivo`, `Pagina`, `PaginaResponse`, `ApiExceptionHandler`, `ErroResponse`, `Clock`.

## Testes (mesma estrutura dos do Boleto)

| Teste | Modelo |
|---|---|
| Builder de cenário válido | `Cenarios` |
| Uma regra por teste, verificando o tipo da exceção | `RegrasBoletoTest` |
| Máquina de estados como tabela | `EstadoBoletoTest` |
| Entidade aplica estado (409) antes das regras (422) | `BoletoTest` |
| Fuso do negócio na factory | `SolicitacaoBoletoFactoryTest` |
| Lock otimista vira `AlteracaoConcorrenteException` | `ConcorrenciaTest` |
| CRUD + ciclo de vida + erros HTTP (status e `codigo`) | `BoletoResourceTest` |

## Armadilhas comuns (não faça)

- Entidade com `@Entity`/setters "para facilitar o mapeamento": a JPA fica no adapter.
- Use case com `if` de regra ou `switch` em status.
- Reutilizar o `*Output` como resposta HTTP, ou a entidade como `*Output`.
- `ExceptionMapper` por exceção: o handler mapeia por família.
- Interface para cada use case, CQRS ou template genérico de UC "para o futuro": só com necessidade real.

## Feche

- Seção do novo agregado no `README.md` (estados, regras, API).
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` (ajuste `BASE` se o pacote raiz mudar) e `mvn test`.
