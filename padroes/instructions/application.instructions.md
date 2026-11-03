---
applyTo: "**/src/main/java/**/application/**/*.java"
---

# Camada de aplicação (use cases)

- **Um use case por ação**, nome `<Verbo><Agregado>UC` (ex.: `CriarContratoUC`), método único `executar`.
- `@ApplicationScoped`; dependências por **construtor** (sem `@Inject` em campo).
- `@Transactional` em toda operação que escreve.
- Roteiro fixo, sem regra de negócio:
  1. converter `*Input` em objetos de domínio (VOs se auto-validam);
  2. buscar **fatos** nos repositórios (`repository.obter`, contagens);
  3. **delegar a decisão** ao domínio (método da entidade / fábrica estática);
  4. `repository.salvar(...)`;
  5. efeitos externos (integrações de `adapter`) **depois** de salvar;
  6. retornar `*Output.de(entidade)`.
- Injete as classes concretas de `adapter` (repositório, integrações) pelo construtor; use só os
  métodos que falam domínio (nunca `persist`/`find` do Panache nem `*JpaEntity`).
- Proibido: `if` que implementa regra de negócio, `switch` em status, acessar `entrypoint`, JPA,
  devolver entidade de domínio, `LocalDate.now()` sem `Clock` injetado.
- DTOs em `application.<agregado>.dto`: `record`s `*Input` (com métodos que montam VOs) e `*Output`
  (com `static de(Entidade)`).
- Se precisar de uma decisão nova: crie a regra no domínio (skill `layered-nova-regra-de-negocio`),
  não aqui.
