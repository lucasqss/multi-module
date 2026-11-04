---
name: layered-novo-agregado-crud
description: Cria um novo agregado com CRUD completo em projeto Java layered (entidade rica, value objects, specifications, State, exceções específicas, repositório JPA concreto, use cases, API REST e testes). Use quando pedirem um novo cadastro, recurso, entidade ou módulo de negócio.
---

# Novo agregado com CRUD completo

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`): um
> processo de pagamento de boletos, que mostra o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

## 0. Pré-requisitos e modelos

1. Leia o **"Mapa do projeto"** em `copilot-instructions.md`/`AGENTS.md`.
2. Se o projeto **já tem um agregado** no padrão, ele é o gabarito: para cada arquivo abaixo,
   **abra o equivalente do agregado existente e copie a forma**. Consistência entre agregados é o objetivo.
3. Se a base genérica (`Specification`, `RegraNegocio`, `ValidadorRegras`, famílias de exceção,
   handler de erros) não existir, rode antes a skill `layered-iniciar-projeto`.

## 1. Desenhe antes de codificar (confirme com o usuário se não estiver claro)

1. **Estados e transições** (diagrama). Sem ciclo de vida? Pule o State.
2. **Partes** do agregado: quais são substituídas inteiras (value objects, ex.: lista de boletos) e quais
   têm identidade e ciclo de vida próprios dentro dele (**entidades internas**, ex.: aprovações, tentativas).
   E se ele pode ficar **incompleto** enquanto é preenchido (padrão `EM_EDICAO`).
3. **Regras de negócio** e em que **momento** valem (salvar uma seção, finalizar, outra ação, fato externo).
4. **Dados**, e quais têm regra de formato (viram value objects).
5. **Ações** além do CRUD (viram `POST /{id}/<substantivo>`) e **integrações** (entrada e saída).
6. **Fatos** que as regras precisam (contagens, dados de outro sistema): virão do use case.

## 2. Arquivos (de dentro para fora)

| # | Papel | Arquivo no seu agregado | Ilustração (processo de pagamento) |
|---|---|---|---|
| 1 | Id e value objects (`record`s que validam; formato inválido → família 400) | `domain/valueobject/<Agregado>Id`, VOs | `ProcessoPagamentoId`, `Matricula`, `ContaDebito`, `LinhaDigitavel` |
| 2 | Partes substituídas inteiras (seções) e cópias de dados externos | `domain/valueobject/` | `BoletosDoProcesso`, `BoletoAPagar` (cópia do boleto do emissor) |
| 3 | **Entidades internas** (identidade dentro do agregado; sem repositório nem UC) | `domain/entity/` | `Aprovacao`, `TentativaPagamento` |
| 4 | Máquina de estados (enum + base `sealed` + um `final` por estado) | `domain/state/` | `StatusProcesso`, `EstadoProcesso`, `EstadoEmEdicao` |
| 5 | Exceções específicas (404, 409, uma por regra 422) | `domain/exception/` | `ProcessoPagamentoNaoEncontradoException`, `ProcessoSemBoletosException` |
| 6 | Candidatos das regras por momento + `*Spec` + catálogo | `domain/specification/<agregado>/` | `SubmissaoProcesso`, `AprovacaoProcesso`, `RetornoPagamento`, `RegrasProcessoPagamento` |
| 7 | Raiz do agregado (construtor privado, `criar`, `reconstituir`, métodos de negócio, sem setters) | `domain/entity/<Agregado>` | `ProcessoPagamento` |
| 8 | Persistência: entidade JPA separada (`@Version`; partes/entidades internas como `@ElementCollection`) + mapper | `adapter/database/` | `ProcessoPagamentoJpaEntity`, `ProcessoPagamentoMapper` |
| 9 | Repositório concreto (Panache) falando domínio; `obter(id)` → 404; `salvar` traduz lock otimista | `adapter/database/<Agregado>Repository` | `ProcessoPagamentoRepository` |
| 10 | DTOs da application (`Input` monta VOs, `Output.de(entidade)`) | `application/<agregado>/dto/` | `AlterarProcessoInput`, `ProcessoPagamentoOutput` |
| 11 | Um use case por intenção | `application/<agregado>/<Verbo><Agregado>UC` | `CriarProcessoPagamentoUC`, `SubmeterProcessoPagamentoUC` |
| 12 | Contrato HTTP próprio + resource fino | `entrypoint/api/v1/...` | `SalvarBoletosRequest`, `ProcessoPagamentoResponse`, `ProcessoPagamentoResource` |

**Ilustração** (esqueleto do `ProcessoPagamento`: agregado com partes, entidades internas, que nasce incompleto):

```java
public class ProcessoPagamento {

    private final ProcessoPagamentoId id;
    private final Matricula solicitante;
    private BoletosDoProcesso boletos;                 // parte (VO), substituída inteira
    private final List<Aprovacao> aprovacoes;          // entidades internas
    private final List<TentativaPagamento> tentativas; // entidades internas
    private EstadoProcesso estado;
    // ... sem setters

    /** Nasce em EM_EDICAO só com o mínimo; completude é exigida em submeter(). */
    public static ProcessoPagamento criar(Matricula solicitante, Instant agora) { ... }

    /** Para o adapter: reconstrói sem revalidar. */
    public static ProcessoPagamento reconstituir(/* campos */, StatusProcesso status, /* ... */) { ... }

    public void salvarBoletos(BoletosDoProcesso novos) {
        estado.validarEdicao();                          // 409 fora de EM_EDICAO
        RegrasProcessoPagamento.BOLETOS.validar(novos);  // 422: regras da SEÇÃO
        this.boletos = novos;                            // pode continuar incompleto
    }

    public void submeter(LocalDate hoje) {
        var proximo = estado.submeter();                                                    // 409
        RegrasProcessoPagamento.SUBMISSAO.validar(new SubmissaoProcesso(boletos, contaDebito, dataPagamento, hoje)); // 422: COMPLETUDE
        estado = proximo;
    }

    public void enviarParaPagamento(Instant agora) {
        estado = estado.enviarParaPagamento();
        tentativas.add(TentativaPagamento.nova(id, tentativas.size() + 1, agora)); // só a raiz cria entidades internas
    }
    // getters de leitura (listas como cópia)
}
```

## Quantos casos de uso?

Um por **intenção** (critério completo na skill `layered-novo-caso-de-uso`, passo 0). Agregado com partes
que pode ficar incompleto: `Criar` (nasce `EM_EDICAO`) → um `Salvar<Secao>` por seção (`PUT` substitui
a seção) → um UC de **finalização** que exige completude e muda o estado. Partes não ganham repositório
nem UC por entidade; o `@Version` fica no agregado.
Ilustração: `CriarProcessoPagamentoUC` → `SalvarBoletosDoProcessoUC`, `AlterarProcessoPagamentoUC` → `SubmeterProcessoPagamentoUC`.

## 3. Testes

| Teste | Cobre | Ilustração (processo de pagamento) |
|---|---|---|
| Builder de cenário válido por padrão | base dos testes de domínio | `Cenarios` |
| Regras do catálogo | uma regra por teste, exceção pelo **tipo**; um `@Nested` por momento | `RegrasProcessoPagamentoTest` |
| Estados | tabela (status × ação) completa | `EstadoProcessoTest` |
| Agregado | aplica estado (409) antes das regras (422); partes, entidades internas, fatos | `ProcessoPagamentoTest` |
| Concorrência (integração) | lock otimista → 409, mesmo editando partes diferentes | `ConcorrenciaTest` |
| API (integração) | CRUD + ações + webhooks + erros (status **e** `codigo`) | `ProcessoPagamentoResourceTest` |

## Armadilhas (não faça)

- `@Entity`/setters na entidade de domínio "para facilitar o mapeamento".
- `if` de regra ou `switch` de status em use case.
- `*Output` como resposta HTTP, ou entidade como `*Output`.
- Mapper HTTP por exceção (o handler mapeia por família).
- Interface "de porta" para repositório/integração, interface por use case, CQRS ou template
  genérico de UC sem necessidade real (mais de uma implementação).

## Feche

Atualize o **Mapa do projeto** (novo agregado, catálogo, estados), a documentação da API, rode a
skill `layered-revisao-arquitetura` e os testes.
