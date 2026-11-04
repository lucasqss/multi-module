---
applyTo: "**/src/main/java/**/domain/**/*.java"
---

# Camada de domínio

Pastas permitidas: `entity`, `valueobject`, `exception`, `specification` (+ `specification/<agregado>`)
e `state`. **Não crie** `factory`, `model`, `enums`, `repository`, `gateway` nem `service`
(veja a skill `layered-enxugar-estrutura` para o destino de cada conteúdo).

O domínio é **Java puro**. Proibido importar `jakarta.*`, `io.quarkus.*`, `org.hibernate.*`,
`com.fasterxml.*` ou qualquer pacote `application`, `adapter`, `entrypoint`.
Sem anotações de framework (nem `@ApplicationScoped`): quem liga objetos de domínio ao CDI é
o producer de `Clock` em `shared/` (os use cases recebem o `Clock` injetado).

## Entidades (`domain.entity`) — nunca anêmicas

- Campos `private`; `final` para o que não muda (id, data de criação).
- **Sem setters.** Construtor `private`. Criação por fábrica estática que **valida**
  (ilustração: `Boleto.criar(...)`) e `reconstituir(...)` para o adapter, sem revalidar.
- Métodos com **nome de negócio** e esta ordem fixa:
  ```java
  // ilustração: Boleto.emitir no repositório de referência
  public void emitir(CondicoesEmissao condicoes) {
      var proximo = estado.emitir();                    // 1. estado autoriza (409)
      RegrasBoleto.EMISSAO.validar(candidato);          // 2. specs validam (422)
      estado = proximo;                                 // 3. só então muda
      linhaDigitavel = ...;
  }
  ```
- Getters são permitidos (leitura); `Optional` para campos que podem não existir.
- Se um método só lê campos de OUTRO objeto para decidir algo, a lógica pertence ao outro objeto
  (Information Expert). Ex.: `documento.pessoaFisica()`, não `documento.numero().length() == 11`.

## Agregado com partes e fatos externos

- Partes (itens, parcelas, documentos) só mudam por métodos do agregado (ilustração: `salvarItens`),
  que chamam `estado.validarEdicao()` e as regras **da seção**. Completude só no método de finalização
  (ilustração: `emitir`). Partes não têm repositório nem use case próprios.
- **Fato vindo de fora** (aviso de pagamento, retorno de processamento) não é recusado por regra:
  classifique com `Regras<Agregado>.<CONJUNTO>.primeiraViolacao(...)` e deixe o estado escolher o destino
  (ilustração: `Boleto.registrarPagamento` → `estado.receberPagamento(conforme)`). Ignore o mesmo id
  externo (idempotência).

## Value objects (`domain.valueobject`)

- `record` com construtor compacto que valida e normaliza. Formato inválido → exceção específica
  da família `DadoInvalidoException` (400).
- Imutáveis; operações retornam nova instância (`dados.comValor(novoValor)`).
- Dados sensíveis (CPF/CNPJ, conta) mascarados no `toString()`.

## Specifications (`domain.specification`)

- Regra com erro próprio: `extends RegraNegocio<Candidato>` com `isSatisfiedBy` + `violacao`.
- Bloco de construção sem erro: `implements Specification<Candidato>`, para compor no catálogo
  com `and`/`or`/`not`/`quando(...).entao(...)`.
- **Puras**: sem I/O, sem relógio, sem injeção. Tudo vem no candidato, um `record` na mesma pasta
  das specs (`specification/<agregado>`), criado por métodos estáticos `para<Momento>(...)`.
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

## Sem repositórios nem integrações aqui

- O domínio **não** declara interfaces de repositório/integração (não usamos ports and adapters).
  Persistência e integrações são classes concretas em `adapter/`, injetadas nos use cases.
  Nunca coloque repositório Panache/JPA dentro de `domain`: o domínio não depende de framework.
- Lógica entre várias entidades que não pertence a nenhuma: serviço de domínio em `domain.service`, sem framework.
- O domínio só fornece o que elas precisam: entidades com `reconstituir(...)`, VOs e exceções
  específicas (ex.: `<Agregado>NaoEncontradoException`, `AlteracaoConcorrenteException`).
