---
name: novo-agregado-crud
description: Cria um novo agregado com CRUD completo (entidade rica, value objects, specifications, State, exceções específicas, repositório JPA, use cases, API REST e testes) copiando a estrutura do agregado Boleto. Use quando pedirem um novo cadastro/recurso/entidade, ou para replicar este modelo em outro projeto.
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
| 3 | `domain/state/`: enum `Status<Agregado>` (só o rótulo), `Estado<Agregado>` (`sealed`) e um `final` por estado | `StatusBoleto`, `EstadoBoleto`, `EstadoEmitido` |
| 4 | `domain/exception/<Situacao>Exception` (uma por caso, na família certa) | `BoletoNaoEncontradoException`, `BoletoVencidoException` |
| 6 | `domain/specification/<agregado>/`: candidatos por momento (seções, finalização + `Condicoes*`), `...Spec` e catálogo `Regras<Agregado>` | `EmissaoBoleto`, `CondicoesEmissao`, `DestinatariosDoBoleto`, `RegrasBoleto` |
| 7 | `domain/entity/<Agregado>` (rica: fábrica estática, `reconstituir`, sem setters) | `Boleto` |
| 9 | `adapter/database/<Agregado>JpaEntity` (`@Version`), `<Agregado>Mapper` | `BoletoJpaEntity`, `BoletoMapper` |
| 10 | `adapter/database/<Agregado>Repository` (+ `Filtro<Agregado>`): classe concreta que fala domínio, com `obter(id)` | `BoletoRepository`, `FiltroBoleto` |
| 11 | `application/<agregado>/dto/*Input`, `<Agregado>Output` | `CriarBoletoInput`, `SalvarItensBoletoInput`, `BoletoOutput` |
| 12 | `application/<agregado>/<Verbo><Agregado>UC` (um por ação) | `EmitirBoletoUC`, `CancelarBoletoUC` |
| 13 | `entrypoint/api/v1/request/*Request`, `response/<Agregado>Response`, `<Agregado>Resource` | `CriarBoletoRequest`, `SalvarItensBoletoRequest`, `BoletoResponse`, `BoletoResource` |

**Reaproveite, não duplique:** `Specification`, `RegraNegocio`, `ValidadorRegras`, famílias de
exceção, `Dinheiro`, `Motivo`, `Pagina`, `PaginaResponse`, `ApiExceptionHandler`, `ErroResponse`, `Clock`.

## Quantos casos de uso?

Um por **intenção** (critério em "O que é um caso de uso", `.github/copilot-instructions.md`). Se o
agregado tem partes e pode ficar incompleto enquanto é preenchido, siga o padrão do `Boleto`:
`Criar` (nasce `EM_EDICAO`) → um `Salvar<Secao>` por seção (`PUT`) → um UC de finalização que exige
completude (`EmitirBoletoUC`). Partes não ganham repositório nem UC por entidade.

## Testes (mesma estrutura dos do Boleto)

| Teste | Modelo |
|---|---|
| Builder de cenário válido | `Cenarios` |
| Uma regra por teste, verificando o tipo da exceção | `RegrasBoletoTest` |
| Máquina de estados como tabela | `EstadoBoletoTest` |
| Entidade aplica estado (409) antes das regras (422) | `BoletoTest` |
| Fuso do negócio nas condições | `CondicoesEmissaoTest` |
| Lock otimista vira `AlteracaoConcorrenteException` | `ConcorrenciaTest` |
| CRUD + ciclo de vida + erros HTTP (status e `codigo`) | `BoletoResourceTest` |

## Armadilhas comuns (não faça)

- Entidade com `@Entity`/setters "para facilitar o mapeamento": a JPA fica no adapter.
- Use case com `if` de regra ou `switch` em status.
- Reutilizar o `*Output` como resposta HTTP, ou a entidade como `*Output`.
- `ExceptionMapper` por exceção: o handler mapeia por família.
- Interface "de porta" para repositório/integração, interface para cada use case, CQRS ou template
  genérico de UC "para o futuro": só com necessidade real (mais de uma implementação).

## Feche

- Seção do novo agregado no `README.md` (estados, regras, API).
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` (ajuste `BASE` se o pacote raiz mudar) e `mvn test`.
