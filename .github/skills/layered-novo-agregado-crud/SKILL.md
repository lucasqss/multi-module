---
name: layered-novo-agregado-crud
description: Cria um novo agregado com CRUD completo em projeto Java layered (entidade rica, value objects, specifications, State, exceções específicas, repositório JPA concreto, use cases, API REST e testes). Use quando pedirem um novo cadastro, recurso, entidade ou módulo de negócio.
---

# Novo agregado com CRUD completo

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`), que mostra
> o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

## 0. Pré-requisitos e modelos

1. Leia o **"Mapa do projeto"** em `copilot-instructions.md`/`AGENTS.md`.
2. Se o projeto **já tem um agregado** no padrão, ele é o gabarito: para cada arquivo abaixo,
   **abra o equivalente do agregado existente e copie a forma**. Consistência entre agregados é o objetivo.
3. Se a base genérica (`Specification`, `RegraNegocio`, `ValidadorRegras`, famílias de exceção,
   handler de erros) não existir, rode antes a skill `layered-iniciar-projeto`.

## 1. Desenhe antes de codificar (confirme com o usuário se não estiver claro)

1. **Estados e transições** (diagrama). Sem ciclo de vida? Pule o State.
2. **Partes** do agregado (itens, parcelas, documentos) e se ele pode ficar **incompleto** enquanto é
   preenchido (padrão `EM_EDICAO`).
3. **Regras de negócio** e em que **momento** valem (salvar uma seção, finalizar, outra ação, fato externo).
4. **Dados**, e quais têm regra de formato (viram value objects).
5. **Ações** além do CRUD (viram `POST /{id}/<substantivo>`) e **integrações** (entrada e saída).
6. **Fatos** que as regras precisam (contagens, dados de outro sistema): virão do use case.

## 2. Arquivos (de dentro para fora)

| # | Papel | Arquivo no seu agregado | Ilustração (boleto) |
|---|---|---|---|
| 1 | Id e value objects (`record`s que validam; formato inválido → família 400) | `domain/valueobject/<Agregado>Id`, VOs | `BoletoId`, `Pagador`, `ItensBoleto`, `LinhaDigitavel` |
| 2 | Máquina de estados (enum + base `sealed` + um `final` por estado) | `domain/state/` | `StatusBoleto`, `EstadoBoleto`, `EstadoEmEdicao` |
| 3 | Exceções específicas (404, 409, uma por regra 422) | `domain/exception/` | `BoletoNaoEncontradoException`, `BoletoSemItensException` |
| 4 | Candidatos das regras por momento + `*Spec` + catálogo | `domain/specification/<agregado>/` | `EmissaoBoleto` + `CondicoesEmissao`, `ItensBoleto`, `RegrasBoleto` |
| 5 | Entidade rica (construtor privado, `criar`, `reconstituir`, métodos de negócio, sem setters) | `domain/entity/<Agregado>` | `Boleto` |
| 6 | Persistência: entidade JPA separada (`@Version`) + mapper | `adapter/database/<Agregado>JpaEntity`, `<Agregado>Mapper` | `BoletoJpaEntity`, `BoletoMapper` |
| 7 | Repositório concreto (Panache) falando domínio; `obter(id)` → 404; `salvar` traduz lock otimista | `adapter/database/<Agregado>Repository` (+ `Filtro<Agregado>`) | `BoletoRepository`, `FiltroBoleto` |
| 8 | DTOs da application (`Input` monta VOs, `Output.de(entidade)`) | `application/<agregado>/dto/` | `CriarBoletoInput`, `BoletoOutput` |
| 9 | Um use case por intenção | `application/<agregado>/<Verbo><Agregado>UC` | `CriarBoletoUC`, `EmitirBoletoUC` |
| 10 | Contrato HTTP próprio + resource fino | `entrypoint/api/v1/request`, `response`, `<Agregado>Resource` | `CriarBoletoRequest`, `BoletoResponse`, `BoletoResource` |

**Ilustração** (esqueleto do `Boleto`, um agregado com partes que nasce incompleto):

```java
public class Boleto {

    private final BoletoId id;
    private final Pagador pagador;
    private ItensBoleto itens;
    private EstadoBoleto estado;
    // ... sem setters

    private Boleto(/* todos os campos */) { ... }

    /** Nasce em EM_EDICAO só com o mínimo; completude é exigida em emitir(). */
    public static Boleto criar(Pagador pagador, LocalDate dataVencimento, Instant agora) { ... }

    /** Para o adapter: reconstrói sem revalidar. */
    public static Boleto reconstituir(/* campos */, StatusBoleto status, /* ... */) { ... }

    public void salvarItens(ItensBoleto novosItens) {
        estado.validarEdicao();                   // 409 fora de EM_EDICAO
        RegrasBoleto.ITENS.validar(novosItens);   // 422: regras da SEÇÃO
        this.itens = novosItens;                  // pode continuar incompleto
    }

    public void emitir(CondicoesEmissao condicoes) {
        var proximo = estado.emitir();                                                      // 409
        RegrasBoleto.EMISSAO.validar(new EmissaoBoleto(pagador, itens, dataVencimento, condicoes)); // 422: COMPLETUDE
        estado = proximo;
        // ...
    }
    // getters de leitura
}
```

## Quantos casos de uso?

Um por **intenção** (critério completo na skill `layered-novo-caso-de-uso`, passo 0). Agregado com partes
que pode ficar incompleto: `Criar` (nasce `EM_EDICAO`) → um `Salvar<Secao>` por seção (`PUT` substitui
a seção) → um UC de **finalização** que exige completude e muda o estado. Partes não ganham repositório
nem UC por entidade; o `@Version` fica no agregado.
Ilustração: `CriarBoletoUC` → `SalvarItensBoletoUC`, `SalvarDestinatariosBoletoUC` → `EmitirBoletoUC`.

## 3. Testes

| Teste | Cobre | Ilustração (boleto) |
|---|---|---|
| Builder de cenário válido por padrão | base dos testes de domínio | `Cenarios` |
| Regras do catálogo | uma regra por teste, exceção pelo **tipo**; um `@Nested` por momento | `RegrasBoletoTest` |
| Estados | tabela (status × ação) completa | `EstadoBoletoTest` |
| Entidade | aplica estado (409) antes das regras (422); partes e completude | `BoletoTest` |
| Relógio | "hoje" no fuso do negócio | `CondicoesEmissaoTest` |
| Concorrência (integração) | lock otimista → 409 | `ConcorrenciaTest` |
| API (integração) | CRUD + ações + erros (status **e** `codigo`) | `BoletoResourceTest` |

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
