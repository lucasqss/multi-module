---
applyTo: "**/src/main/java/**/domain/**/*.java"
---

# Camada de domínio

O domínio é **Java puro**. Proibido importar `jakarta.*`, `io.quarkus.*`, `org.hibernate.*`,
`com.fasterxml.*` ou qualquer pacote `application`, `adapter`, `entrypoint`.
Sem anotações de framework (nem `@ApplicationScoped`): quem liga objetos de domínio ao CDI é
o producer CDI de `shared/config` (ver "Mapa do projeto").

## Entidades (`domain.entity`) — nunca anêmicas

- Campos `private`; `final` para o que não muda (id, data de criação).
- **Sem setters.** Construtor `private`. Criação por fábrica estática que **valida**
  (`Contrato.criar(SolicitacaoContrato)`) e `reconstituir(...)` para o adapter, sem revalidar.
- Métodos com **nome de negócio** e esta ordem fixa:
  ```java
  public void aprovar(Aprovacao a, LocalDate hoje) {
      var proximo = estado.aprovar();                   // 1. estado autoriza (409)
      RegrasContrato.APROVACAO.validar(candidato);      // 2. specs validam (422)
      estado = proximo;                                 // 3. só então muda
      aprovacao = a;
  }
  ```
- Getters são permitidos (leitura); `Optional` para campos que podem não existir.
- Se um método só lê campos de OUTRO objeto para decidir algo, a lógica pertence ao outro objeto
  (Information Expert). Ex.: `documento.pessoaFisica()`, não `documento.numero().length() == 11`.

## Value objects (`domain.valueobject`)

- `record` com construtor compacto que valida e normaliza. Formato inválido → exceção específica
  da família `DadoInvalidoException` (400).
- Imutáveis; operações retornam nova instância (`dados.comValor(novoValor)`).
- Dados sensíveis (CPF/CNPJ, conta) mascarados no `toString()`.

## Specifications (`domain.specification`)

- Regra com erro próprio: `extends RegraNegocio<Candidato>` com `isSatisfiedBy` + `violacao`.
- Bloco de construção sem erro: `implements Specification<Candidato>`, para compor no catálogo
  com `and`/`or`/`not`/`quando(...).entao(...)`.
- **Puras**: sem I/O, sem relógio, sem injeção. Tudo vem no candidato (`domain.model`).
- Registradas no catálogo de regras do agregado (`Regras<Agregado>`); a ordem da lista é a prioridade do erro (fail-fast).

## State (`domain.state`)

- A base de estados é `sealed` e lança `TransicaoEstadoInvalidaException` em toda ação.
- Cada estado concreto é `final`, singleton (`INSTANCIA`), e sobrescreve **só** o permitido.
- Transições **retornam** o próximo estado. Consultas (`emAberto()`) moram no estado, não em `if`s.

## Exceções (`domain.exception`)

- Uma classe por situação; `public static final String CODIGO`; dados do erro no construtor;
  mensagem montada dentro da exceção.
- Estenda a família certa: `DadoInvalidoException`, `RecursoNaoEncontradoException`,
  `ConflitoException`, `RegraNegocioException`. Nunca estenda `DomainException` diretamente.

## Portas

- Persistência em `domain.repository`; integração externa em `domain.gateway` (ou onde o projeto já as coloca).
- Lógica entre várias entidades que não pertence a nenhuma: serviço de domínio em `domain.service`, sem framework.
- Interfaces com tipos de domínio. `default obter(id)` lança a exceção `*NaoEncontrado` específica.
