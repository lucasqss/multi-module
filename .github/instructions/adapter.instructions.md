---
applyTo: "src/main/java/**/adapter/**/*.java"
---

# Camada de adapters

- Classes concretas (`@ApplicationScoped`) **injetadas diretamente nos use cases**; sem interface "de porta".
  Nunca chamadas por `entrypoint` e nunca importam `application`/`entrypoint`.
- **Falam a língua do domínio**: métodos públicos recebem/devolvem entidades e VOs de domínio, nunca
  `*JpaEntity` ou DTO externo. Repositório tem `obter(id)` que lança a exceção `*NaoEncontrado` específica.
- **Persistência:** `*JpaEntity` separada da entidade de domínio (anêmica de propósito: é o formato
  da tabela), `@Version` para lock otimista, `*Mapper` package-private com `copiar(dominio, jpa)` e
  `paraDominio(jpa)` usando `Entidade.reconstituir(...)`.
- `salvar` faz `flush()` e traduz `OptimisticLockException` para `AlteracaoConcorrenteException`.
  Exceções de framework **não vazam** do adapter: traduza para exceções de domínio.
- Não duplique regra: se a query depende de um conceito do domínio (ex.: "em aberto"), derive-o do
  domínio (`EstadoBoleto.de(status).emAberto()`), como em `BoletoRepository.STATUS_EM_ABERTO`.
- Integração simulada deixa isso explícito no Javadoc (ex.: `NotificadorBoleto`).
- Interface só quando houver **mais de uma implementação real** em produção.
