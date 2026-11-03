---
applyTo: "src/main/java/**/application/**/*.java"
---

# Camada de aplicação (use cases)

- **Um use case por ação**, nome `<Verbo><Agregado>UC` (`EmitirBoletoUC`), método único `executar`.
- `@ApplicationScoped`; dependências por **construtor** (sem `@Inject` em campo).
- `@Transactional` em toda operação que escreve.
- Roteiro fixo, sem regra de negócio:
  1. converter `*Input` em objetos de domínio (VOs se auto-validam);
  2. buscar **fatos** nas portas (`repository.obter`, contagens);
  3. **delegar a decisão** ao domínio (método da entidade / fábrica estática);
  4. `repository.salvar(...)`;
  5. efeitos externos (gateways) **depois** de salvar;
  6. retornar `*Output.de(entidade)`.
- Proibido: `if` que implementa regra de negócio, `switch` em status, acessar `adapter`/`entrypoint`,
  devolver entidade de domínio, `LocalDate.now()` sem `Clock` injetado.
- DTOs em `application.<agregado>.dto`: `record`s `*Input` (com métodos que montam VOs) e `*Output`
  (com `static de(Entidade)`).
- Se precisar de uma decisão nova: crie a regra no domínio (skill `nova-regra-de-negocio`),
  não aqui.
