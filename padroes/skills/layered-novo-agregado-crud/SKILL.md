---
name: layered-novo-agregado-crud
description: Cria um novo agregado com CRUD completo em projeto Java layered (entidade rica, value objects, specifications, State, exceções específicas, portas, adapter JPA, use cases, API REST e testes). Use quando pedirem um novo cadastro, recurso, entidade ou módulo de negócio.
---

# Novo agregado com CRUD completo

## 0. Pré-requisitos e modelos

1. Leia o **"Mapa do projeto"** em `copilot-instructions.md`/`AGENTS.md`.
2. Se o projeto **já tem um agregado** no padrão, ele é o gabarito: para cada arquivo abaixo,
   **abra o equivalente do agregado existente e copie a forma**. Consistência entre agregados é o objetivo.
3. Se a base genérica (`Specification`, `RegraNegocio`, `ValidadorRegras`, famílias de exceção,
   handler de erros) não existir, rode antes a skill `layered-iniciar-projeto`.

## 1. Desenhe antes de codificar (confirme com o usuário se não estiver claro)

1. **Estados e transições** (diagrama). Sem ciclo de vida? Pule o State.
2. **Regras de negócio** e em que momento valem (criação, alteração, cada ação).
3. **Dados**, e quais têm regra de formato (viram value objects).
4. **Ações** além do CRUD (viram `POST /{id}/<substantivo>`).
5. **Fatos externos** que as regras precisam (contagens, outro sistema): virão do use case.

## 2. Arquivos (de dentro para fora). Exemplo com o agregado `Contrato`

| # | Arquivo | Conteúdo |
|---|---|---|
| 1 | `domain/valueobject/ContratoId` + VOs | `record`s que validam; formato inválido → exceção da família 400 |
| 2 | `domain/enums/StatusContrato` | só o rótulo |
| 3 | `domain/state/EstadoContrato` + um `final` por estado | skill `layered-novo-estado` (passo C) |
| 4 | `domain/exception/*Exception` | uma por situação: `ContratoNaoEncontradoException` (404), `TransicaoEstadoInvalidaException`/`AlteracaoConcorrenteException` (409), uma por regra (422) |
| 5 | `domain/model/SolicitacaoContrato` | candidato das regras: dados + momento + hoje + parâmetros + fatos |
| 6 | `domain/specification/contrato/*Spec` + `RegrasContrato` | skill `layered-nova-regra-de-negocio` |
| 7 | `domain/entity/Contrato` | rica: construtor privado, `criar(Solicitacao)` que valida, `reconstituir(...)`, métodos de negócio, sem setters |
| 8 | `domain/factory/SolicitacaoContratoFactory` + registro no producer CDI | monta o candidato com `Clock` e configuração |
| 9 | `domain/repository/ContratoRepository` (+ `FiltroContrato`) | `salvar`, `buscarPorId`, `listar(filtro, pagina, tamanho)`, `remover`, `default obter(id)` → 404 |
| 10 | `adapter/database/ContratoJpaEntity`, `ContratoMapper`, `ContratoRepositoryPanache` | JPA separada com `@Version`; `salvar` faz `flush` e traduz `OptimisticLockException` → `AlteracaoConcorrenteException` |
| 11 | `application/contrato/dto/*Input`, `ContratoOutput` | `record`s; Input monta VOs; `Output.de(entidade)` |
| 12 | `application/contrato/<Verbo>ContratoUC` | um por ação, `@Transactional` quando escreve (skill `layered-novo-caso-de-uso`) |
| 13 | `entrypoint/api/v1/request/*Request`, `response/ContratoResponse`, `ContratoResource` | Request sintático, Response próprio, resource fino |

Esqueleto da entidade (passo 7):

```java
public class Contrato {

    private final ContratoId id;
    private DadosContrato dados;
    private EstadoContrato estado;

    private Contrato(ContratoId id, DadosContrato dados, EstadoContrato estado) { ... }

    public static Contrato criar(SolicitacaoContrato s) {
        RegrasContrato.CRIACAO.validar(s);
        return new Contrato(ContratoId.novo(), s.dados(), EstadoContrato.inicial());
    }

    public static Contrato reconstituir(ContratoId id, DadosContrato dados, StatusContrato status) {
        return new Contrato(id, dados, EstadoContrato.de(status));
    }

    public void alterar(SolicitacaoContrato s) {
        estado.validarAlteracao();            // 409
        RegrasContrato.ALTERACAO.validar(s);  // 422
        this.dados = s.dados();
    }
    // getters de leitura; nenhum setter
}
```

## 3. Testes

| Teste | Cobre |
|---|---|
| Builder `Cenarios` (cenário válido por padrão) | base dos testes de domínio |
| `RegrasContratoTest` | uma regra por teste, exceção pelo **tipo** |
| `EstadoContratoTest` | tabela (status × ação) completa |
| `ContratoTest` | entidade aplica estado (409) antes das regras (422) |
| `SolicitacaoContratoFactoryTest` | "hoje" no fuso do negócio |
| `ConcorrenciaTest` (integração) | lock otimista → 409 |
| `ContratoResourceTest` (integração) | CRUD + ações + erros (status **e** `codigo`) |

## Armadilhas (não faça)

- `@Entity`/setters na entidade de domínio "para facilitar o mapeamento".
- `if` de regra ou `switch` de status em use case.
- `*Output` como resposta HTTP, ou entidade como `*Output`.
- Mapper HTTP por exceção (o handler mapeia por família).
- Interface por use case, CQRS ou template genérico de UC sem necessidade real.

## Feche

Atualize o **Mapa do projeto** (novo agregado, catálogo, estados), a documentação da API, rode a
skill `layered-revisao-arquitetura` e os testes.
