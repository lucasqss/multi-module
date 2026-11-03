---
name: novo-caso-de-uso
description: Cria uma nova ação de ponta a ponta no projeto (método de negócio na entidade, use case, DTOs Input/Output, Request/Response, endpoint REST e testes), respeitando a arquitetura em camadas. Use quando pedirem uma nova operação, endpoint, comando ou consulta sobre boletos.
---

# Novo caso de uso (de ponta a ponta)

## 0. Isto é UM caso de uso? (decida antes de codificar)

Siga a seção **"O que é um caso de uso"** de `.github/copilot-instructions.md`. Em resumo:

- Um caso de uso = uma **intenção** do usuário/sistema ("o <ator> quer <verbo> <algo>"), um `executar`,
  uma transação. **Nunca** acrescente um segundo método público a um UC existente.
- Mexe numa **parte** do boleto (itens, destinatários)? Não crie UC por item nem por entidade:
  é um UC por **seção** (`SalvarItensBoletoUC`), que substitui a seção inteira e passa pelo `Boleto`.
- A ação **finaliza** algo e exige completude? É um UC de finalização (`EmitirBoletoUC`), com as regras de
  finalização no catálogo.
- Separe quando a regra, a permissão, a transição ou o ator mudam; junte quando sempre andam juntos.

Construa **de dentro para fora**: domínio → application → entrypoint → testes. Cada camada só conhece
a de dentro.

## 1. Domínio (comportamento primeiro)

- A decisão é da **entidade** (`domain/entity/Boleto.java`): crie um método com nome de negócio.
  Se o status importa, ele chama o estado (skill `novo-estado`); se há regra sobre dados, as specs
  (skill `nova-regra-de-negocio`). Ordem: estado (409) → specs (422) → muda estado.
- Dados novos com regra de formato viram **value object** em `domain/valueobject` (`record` que valida).
- Precisa de dados persistidos de outra forma? Adicione o método em `adapter/database/BoletoRepository`
  (recebendo/devolvendo tipos de domínio). Integração externa: skill `nova-integracao-externa`.
- Consulta pura (sem mudança de estado) pode pular este passo.

## 2. Application

`src/main/java/com/example/boleto/application/boleto/<Verbo>BoletoUC.java`. Modelos:
`ReceberPagamentoBoletoUC` (com entrada), `CancelarBoletoUC` (simples), `NotificarBoletoUC` (com integração externa),
`SalvarItensBoletoUC` (seção do agregado), `EmitirBoletoUC` (finalização com completude).

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
