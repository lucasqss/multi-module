---
applyTo: "src/main/java/**/adapter/**/*.java"
---

# Camada de adapters

- Implementa portas de `domain.repository` / `domain.gateway`. Nunca chamada por `entrypoint` diretamente.
- **Persistência:** `*JpaEntity` separada da entidade de domínio (anêmica de propósito: é o formato
  da tabela), `@Version` para lock otimista, `*Mapper` package-private com `copiar(dominio, jpa)` e
  `paraDominio(jpa)` usando `Entidade.reconstituir(...)`.
- `salvar` faz `flush()` e traduz `OptimisticLockException` para `AlteracaoConcorrenteException`.
  Exceções de framework **não vazam** do adapter: traduza para exceções de domínio.
- Não duplique regra: se a query depende de um conceito do domínio (ex.: "em aberto"), derive-o do
  domínio (`EstadoBoleto.de(status).emAberto()`), como em `BoletoRepositoryPanache.STATUS_EM_ABERTO`.
- Integrações simuladas ficam explícitas no nome (`*Fake`) e no Javadoc.
