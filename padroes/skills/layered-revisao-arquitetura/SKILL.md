---
name: layered-revisao-arquitetura
description: Revisa código Java organizado em camadas (domain, application, adapter, entrypoint, ligadas por injeção de dependência, sem ports and adapters) contra a regra de dependência, domínio rico (não anêmico), SOLID, GRASP, Specification e State. Use ao revisar PR ou diff, antes de concluir uma mudança, ou quando pedirem "revise", "está no padrão?", "tem domínio anêmico?" ou "refatore".
---

# Revisão de arquitetura (projetos layered)

## 0. Contexto do projeto

Se o `copilot-instructions.md` (ou `AGENTS.md`) do repositório tiver a seção **"Mapa do projeto"**,
use os nomes de lá. Senão, descubra o pacote base: é o diretório sob `src/main/java` que contém
`domain/` e `application/` (o script abaixo mostra qual encontrou).

## 1. Verificação automática (sempre primeiro)

Na raiz do repositório, rode o script que acompanha esta skill (`verificar-camadas.sh`, nesta mesma pasta):

```bash
bash <pasta-desta-skill>/verificar-camadas.sh            # descobre o pacote base sozinho
bash <pasta-desta-skill>/verificar-camadas.sh src/main/java/<pacote/base>   # ou informe
```

Ele checa imports entre camadas, setters/construtores públicos em entidades, `now()` sem `Clock` e
`if`/`switch` em status. Qualquer ✗ é bloqueante. Depois rode os testes do projeto (`mvn test`).

## 2. Checklist manual

Para cada violação: `arquivo:linha`, princípio violado e correção concreta.

### Camadas
- [ ] `domain` sem framework e sem importar `application`/`adapter`/`entrypoint`.
- [ ] `application` conhece `domain` e injeta classes concretas de `adapter`; nunca `entrypoint`
      nem JPA direto (só os métodos de domínio do repositório).
- [ ] `entrypoint` não acessa `adapter`. Vale para **todos** os entrypoints (REST `api.v1`,
      mensageria/IIB `iib.v1`, consumidores de fila): todos chamam os mesmos use cases.
- [ ] Contratos separados: `*Request`/`*Response` (entrypoint) ≠ `*Input`/`*Output` (application)
      ≠ entidade (domain) ≠ `*JpaEntity`/DTO externo (adapter).
- [ ] Adapter traduz exceções de framework/integração para exceções de domínio.

### Domínio rico (anti-anêmico)
- [ ] Entidade sem setters e sem construtor público; criação por fábrica estática que valida;
      `reconstituir(...)` para o adapter.
- [ ] Métodos com nome de negócio (`cancelar`, `aprovar`), não de CRUD (`setStatus`, `atualizar`).
- [ ] Ordem nas operações: estado autoriza (409) → specs validam (422) → muda estado.
- [ ] Conceitos com regra de formato são value objects (`record` que valida), não `String`/`BigDecimal` soltos.
- [ ] **Tell, don't ask**: código que lê getters de um objeto para decidir por ele → mova a decisão
      para o dono do dado (Information Expert).
- [ ] Lógica que envolve várias entidades e não pertence a nenhuma → serviço de domínio
      (`domain/service`), sem framework. Não a coloque no use case.

### Use cases
- [ ] Só orquestra: buscar fatos nos repositórios → domínio decide → salvar → efeito externo → `*Output`.
- [ ] Nenhum `if` de regra de negócio; nenhum `switch` em status.
- [ ] Efeitos externos depois de salvar; risco de "enviou e o commit falhou" documentado (Outbox).

### Specification
- [ ] Regra de negócio = spec (`RegraNegocio`/`Specification`) registrada no catálogo de regras,
      com exceção específica.
- [ ] Spec pura: sem I/O, sem `Clock`, sem injeção. Fatos chegam no candidato.
- [ ] Regra condicional por composição (`quando(...).entao(...)`, `and`/`or`/`not`), não `if` na spec.

### State
- [ ] Comportamento dependente de status em classes de estado (`sealed`, proíbe tudo por padrão).
- [ ] Transição nova coberta pelo teste de tabela de transições.

### Exceções
- [ ] Uma exceção por situação, na família certa (`DadoInvalido` 400, `RecursoNaoEncontrado` 404,
      `Conflito` 409, `RegraNegocio` 422), com `CODIGO` fixo.
- [ ] Nenhum mapper HTTP por exceção: o handler mapeia por família.
- [ ] Erro de programação = `IllegalArgumentException`/`IllegalStateException`.

### SOLID / GRASP
- **SRP**: um motivo para mudar por classe. **OCP**: regra/estado novo sem editar UCs nem handler.
- **LSP**: estados concretos não quebram o contrato da base. **ISP/coesão**: repositórios e integrações
  separados por responsabilidade.
- **Dependências**: sem ports and adapters; use cases injetam classes concretas de `adapter`.
  Interface com uma única implementação "para o futuro" é problema. O domínio nunca depende de `adapter`/framework.
- **Creator, Information Expert, Low Coupling, High Cohesion, Protected Variations, Indirection.**

### Testes
- [ ] Domínio com JUnit puro (sem subir framework) e builder de cenário válido.
- [ ] Exceção verificada pelo **tipo**; teste de API verifica status **e** `codigo`.

## 3. Formato da resposta

1. Resultado do script e dos testes.
2. Violações por gravidade: **bloqueante** (camadas, domínio anêmico, regra em UC/entrypoint) →
   **importante** (SOLID/GRASP, testes faltando) → **sugestão** (nomes, legibilidade).
3. Cada uma: `arquivo:linha` — problema — princípio — correção (com trecho de código).
4. Não proponha abstração especulativa (interface "de porta" ou para UC com uma implementação, CQRS,
   template genérico de UC). Simplicidade também é critério.
