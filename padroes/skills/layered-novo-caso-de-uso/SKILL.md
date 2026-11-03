---
name: layered-novo-caso-de-uso
description: Cria uma nova ação de ponta a ponta em projeto Java layered (comportamento na entidade, use case, DTOs Input/Output, Request/Response, endpoint e testes). Use quando pedirem uma nova operação, endpoint, comando, consulta ou consumo de mensagem.
---

# Novo caso de uso (de ponta a ponta)

Construa **de dentro para fora**: domínio → application → entrypoint → testes.

## 0. Localize os modelos

Pelo **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou buscando: classes `*UC` em
`application/`, resources/consumers em `entrypoint/`, `*Request`/`*Response`, `*Input`/`*Output`.
**Abra um use case parecido e copie a forma** (nomes, injeção, anotações, retorno).

## 1. Domínio (comportamento primeiro)

- A **decisão** é da entidade: método com nome de negócio. Status importa → estado
  (skill `layered-novo-estado`); regra sobre dados → specs (skill `layered-nova-regra-de-negocio`).
  Ordem: estado (409) → specs (422) → muda estado.
- Dado novo com regra de formato → value object (`record` que valida).
- Lógica entre várias entidades que não pertence a nenhuma → serviço de domínio (`domain/service`).
- Persistência nova → método na porta de repositório + adapter. Sistema externo → skill
  `layered-nova-integracao-externa`.
- Consulta pura pode pular este passo.

## 2. Application

`application/<agregado>/<Verbo><Agregado>UC.java`. Exemplo com o agregado **Contrato**:

```java
@ApplicationScoped
public class AprovarContratoUC {

    private final ContratoRepository repository;

    public AprovarContratoUC(ContratoRepository repository) {   // injeção por construtor
        this.repository = repository;
    }

    @Transactional
    public ContratoOutput executar(UUID id, String motivo) {
        var agregado = repository.obter(new ContratoId(id));    // 404 automático
        agregado.aprovar(new Motivo(motivo));                     // domínio decide
        repository.salvar(agregado);                              // detecta concorrência
        return ContratoOutput.de(agregado);                     // nunca a entidade
    }
}
```

- Entrada com vários campos → `record *Input` em `application/<agregado>/dto`, com métodos que
  montam VOs. Saída → `*Output` com `static de(Entidade)`.
- "Hoje"/"agora" → injete `Clock`. Efeito externo → **depois** de `salvar`.
- **Proibido**: `if` de regra de negócio, `switch` em status, importar `adapter`/`entrypoint`.

## 3. Entrypoint(s)

- **REST**: CRUD nos verbos (`GET`, `PUT /{id}`, `DELETE /{id}`); evento de negócio/transição como
  `POST /{id}/<substantivo>` (`/aprovacao`, `/cancelamento`).
- **Mensageria/IIB/fila**: consumidor no seu pacote de entrypoint (`iib.v1`, etc.), chamando o
  **mesmo** use case. Nunca duplique lógica entre entrypoints.
- `*Request`: `record` com Bean Validation **só sintática** (`@NotNull`, `@NotBlank`) e `paraInput(...)`.
- `*Response`: contrato próprio com `static de(Output)`; enums viram `String`.
- **Não** crie mapper de exceção: o handler do projeto mapeia por família.

## 4. Testes

- Domínio (JUnit puro): comportamento + ordem 409/422; tabela de estados e regras, se mudaram.
- Entrypoint: caminho feliz (status + campos) e erros (status **e** `codigo`), com dados isolados por teste.

## 5. Feche

Documente o endpoint, rode a skill `layered-revisao-arquitetura` e os testes.
