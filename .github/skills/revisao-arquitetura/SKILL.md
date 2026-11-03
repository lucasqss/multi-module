---
name: revisao-arquitetura
description: Revisa código deste projeto contra a arquitetura em camadas, domínio rico (não anêmico), SOLID, GRASP, Specification e State. Use ao revisar um PR ou diff, antes de concluir qualquer mudança, ou quando pedirem "revise", "está seguindo o padrão?", "refatore" ou "tem domínio anêmico?".
---

# Revisão de arquitetura

## 1. Verificação automática (sempre primeiro)

Na raiz do repositório:

```bash
.github/skills/revisao-arquitetura/verificar-camadas.sh
mvn test
```

O script checa a regra de dependência entre camadas, setters/construtores públicos em entidades,
`now()` sem `Clock` e `if`/`switch` em status. Qualquer ✗ é um problema bloqueante.

## 2. Checklist manual

Para cada item violado, aponte arquivo:linha, o princípio e a correção concreta.

### Camadas
- [ ] Cada classe está na camada certa? Regra de negócio fora de `domain` é violação.
- [ ] O entrypoint devolve `*Response` (nunca `*Output` nem entidade)? O UC devolve `*Output`?
- [ ] O adapter traduz exceções de framework para exceções de domínio?

### Domínio rico (anti-anêmico)
- [ ] Entidade sem setters e sem construtor público; criação por fábrica estática que valida?
- [ ] Métodos com nome de negócio (`cancelar`), não de CRUD (`atualizarStatus`)?
- [ ] Ordem na entidade: estado autoriza (409) → specs validam (422) → muda estado?
- [ ] Conceitos com regra de formato são value objects (não `String`/`BigDecimal` soltos)?
- [ ] **Tell, don't ask**: algum código lê getters de um objeto para decidir por ele? Mova a decisão
      para o objeto dono do dado (Information Expert).

### Use cases
- [ ] O UC só orquestra (buscar fatos → domínio decide → salvar → efeito externo → Output)?
- [ ] Algum `if` no UC é regra de negócio? → vire spec (skill `nova-regra-de-negocio`).
- [ ] Efeitos externos (integrações de `adapter`) acontecem **depois** de `salvar`?

### Specification
- [ ] Regra nova é `RegraNegocio` no catálogo `RegrasBoleto`, com exceção específica?
- [ ] A spec é pura (sem I/O, sem `Clock`, sem injeção)? Fatos vêm no candidato?
- [ ] Regra condicional usa composição (`quando(...).entao(...)`) em vez de `if` dentro da spec?

### State
- [ ] Comportamento dependente de status está numa classe de `domain.state`, não em `if`/`switch`?
- [ ] O estado novo/alterado está na tabela `PERMITIDAS` de `EstadoBoletoTest`?

### Exceções
- [ ] Uma exceção específica por situação, na família certa, com `CODIGO` fixo?
- [ ] Nenhum `ExceptionMapper` novo (o `ApiExceptionHandler` mapeia por família)?
- [ ] Erro de programação usa `IllegalArgumentException`, não `DomainException`?

### SOLID / GRASP
- **SRP**: a classe tem um único motivo para mudar? (factory que monta e também persiste = violação)
- **OCP**: regra/estado novo entrou **sem** editar UCs, handler ou outras regras?
- **LSP**: estados concretos só ampliam o que a base permite, sem quebrar contratos?
- **ISP / coesão**: repositório e integrações separados por responsabilidade (`BoletoRepository` ≠ `NotificadorBoleto`)?
- **Dependências**: o projeto **não** usa ports and adapters; use cases injetam classes concretas de
  `adapter`. Aponte como problema uma interface com uma única implementação criada "para o futuro".
  O domínio, esse sim, nunca depende de `adapter`/framework.
- **Creator / Information Expert / Low Coupling / Protected Variations**: quem cria tem os dados?
  Quem decide tem a informação? Contratos externos isolados por DTO dentro do adapter?

### Testes
- [ ] Domínio testado com JUnit puro e `Cenarios`; exceção verificada pelo **tipo**?
- [ ] Teste de API verifica status **e** `codigo`?

## 3. Formato da resposta

1. Resultado do script e do `mvn test`.
2. Violações ordenadas por gravidade: **bloqueante** (camadas, domínio anêmico, regra no UC) →
   **importante** (SOLID/GRASP, testes faltando) → **sugestão** (nomes, legibilidade).
3. Para cada uma: `arquivo:linha` — problema — princípio — correção proposta (com trecho de código).
4. Não proponha abstrações especulativas (interfaces de UC com uma implementação, CQRS, templates
   genéricos para UCs pequenos). Simplicidade também é critério.
