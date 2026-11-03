---
applyTo: "src/main/java/**/entrypoint/**/*.java"
---

# Camada de entrypoint (HTTP)

- Resource **fino**: converte `*Request` → `*Input`, chama o UC, converte `*Output` → `*Response`.
  Nenhuma regra de negócio, nenhum acesso a repositório/adapter.
- Versionado no caminho (`/api/v1/...`) e no pacote (`entrypoint.api.v1`).
- `*Request`: `record` com Bean Validation **só sintática** (`@NotNull`, `@NotBlank`) e método
  `paraInput(...)`. Regra de negócio é do domínio (422), não daqui.
- `*Response`: contrato próprio, com `static de(Output)`; enums viram `String`. Não reutilize o Output.
- CRUD nos verbos HTTP; eventos de negócio/transições como `POST /{id}/<substantivo>`
  (`/notificacao`, `/pagamento`, `/cancelamento`).
- Erros: **não** crie `ExceptionMapper` novo. `ApiExceptionHandler` já mapeia por família
  (400/404/409/422) e garante o formato único `ErroResponse`. Exceção nova de domínio já sai certa.
