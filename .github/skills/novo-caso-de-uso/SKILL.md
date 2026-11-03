---
name: novo-caso-de-uso
description: Cria uma nova ação de ponta a ponta no projeto (método de negócio na entidade, use case, DTOs Input/Output, Request/Response, endpoint REST e testes), respeitando a arquitetura em camadas. Use quando pedirem uma nova operação, endpoint, comando ou consulta sobre boletos.
---

# Novo caso de uso (de ponta a ponta)

Construa **de dentro para fora**: domínio → application → entrypoint → testes. Cada camada só conhece
a de dentro.

## 1. Domínio (comportamento primeiro)

- A decisão é da **entidade** (`domain/entity/Boleto.java`): crie um método com nome de negócio.
  Se o status importa, ele chama o estado (skill `novo-estado`); se há regra sobre dados, as specs
  (skill `nova-regra-de-negocio`). Ordem: estado (409) → specs (422) → muda estado.
- Dados novos com regra de formato viram **value object** em `domain/valueobject` (`record` que valida).
- Precisa de dados persistidos de outra forma? Adicione o método na porta `BoletoRepository`
  (e implemente em `adapter/database/BoletoRepositoryPanache`). Integração externa: skill `nova-integracao-externa`.
- Consulta pura (sem mudança de estado) pode pular este passo.

## 2. Application

`src/main/java/com/example/boleto/application/boleto/<Verbo>BoletoUC.java`. Modelos:
`ReceberPagamentoBoletoUC` (com entrada), `CancelarBoletoUC` (simples), `NotificarBoletoUC` (com gateway).

```java
@ApplicationScoped
public class EstornarBoletoUC {

    private final BoletoRepository repository;

    public EstornarBoletoUC(BoletoRepository repository) {   // injeção por construtor
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(UUID id, String motivo) {
        var boleto = repository.obter(new BoletoId(id));       // 404 automático
        boleto.estornar(new Motivo(motivo));                   // domínio decide
        repository.salvar(boleto);                             // detecta concorrência (409)
        return BoletoOutput.de(boleto);                        // nunca devolva a entidade
    }
}
```

- Entrada com vários campos → `record <Verbo>BoletoInput` em `application/boleto/dto`, com métodos
  que montam VOs (modelo: `ReceberPagamentoInput.pagamento()`).
- Precisa de "hoje"/"agora"? Injete `Clock` (nunca `now()` sem ele).
- **Proibido**: `if` de regra de negócio, `switch` em status, importar `adapter`/`entrypoint`.

## 3. Entrypoint

- Em `entrypoint/api/v1/BoletoResource.java`, injete o UC pelo construtor e crie o método.
  - CRUD: verbo HTTP no recurso (`GET`, `PUT /{id}`, `DELETE /{id}`).
  - Evento de negócio / transição: `POST /{id}/<substantivo>` (`/estorno`, `/pagamento`).
- Corpo de entrada → `record <Acao>Request` em `request/`, só com Bean Validation **sintática**
  (`@NotNull`, `@NotBlank`) e `paraInput(...)`. Motivo simples reutiliza `MotivoRequest`.
- Saída → `BoletoResponse.de(output)` (ou um `*Response` novo, com `static de(Output)`).
- **Não** crie `ExceptionMapper`: o `ApiExceptionHandler` já mapeia por família.

## 4. Testes

- Domínio: `BoletoTest` (comportamento + ordem 409/422) e, se houver, `EstadoBoletoTest`/`RegrasBoletoTest`.
- API: `BoletoResourceTest`, com um documento de pagador exclusivo do teste: caminho feliz
  (status + campos) e erros (status **e** `codigo`).

## 5. Feche

- Atualize a tabela "API" do `README.md`.
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
