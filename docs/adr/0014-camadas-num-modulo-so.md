# 0014. Camadas num módulo só, com as entidades como modelo

- **Status:** Aceita
- **Data:** 2026-10-28

Substitui as ADRs [0001](0001-multi-module-com-deploy-unico.md),
[0003](0003-heranca-joined-e-genericos-no-dominio.md), [0004](0004-escrita-e-leitura-separadas.md),
[0005](0005-interface-so-onde-ha-costura-real.md), [0008](0008-sistemas-externos-atras-de-ports.md) e
[0011](0011-entradas-por-canal-em-adapter-inbound.md). Mantém o monolito da [0013](0013-monolito-modular-para-o-consorcio.md):
muda só a organização por dentro do jar.

## Contexto

A versão anterior combinava dois estilos: 5 módulos Maven e, dentro deles, o hexagonal. Eram 97 classes em **48
pacotes**:

- pastas `domain/application/port/out/port/spi/adapter/outbound/persistence/mapper/config` repetidas por segmento;
- ports com uma implementação só;
- `Processo<D>` genérico;
- records do domínio separados das entidades, com mappers;
- enums espelhados entre o domínio e o `jpa-core`.

A equipe achou confuso. Além disso, um campo novo atravessava seis lugares: migration, entidade, record, mapper,
projeção e DTO. Com o domínio mudando a cada modalidade de consórcio (ADR 0013), esse é o custo que mais aparece.

O que se quer separar é técnico: a lógica do sistema, a de sistemas externos, a de banco e a de apresentação. É o
particionamento técnico do estilo em camadas (Richards e Ford, *Fundamentals of Software Architecture*).

## Decisão

Um módulo Maven, com cinco pacotes de camada:

| Camada | O que tem | Pode usar |
|---|---|---|
| `apresentacao` | REST e fila, um pacote por canal; só traduz | `negocio`, `persistencia.consulta`, `modelo` |
| `negocio` | o motor (`ProcessoService`), uma pasta por modalidade, cadastros simples | `integracao`, `persistencia`, `modelo` |
| `integracao` | um pacote por sistema externo: cliente HTTP + `*Integracao` | `modelo` |
| `persistencia` | `repositorio` (gravar e ler entidades) e `consulta` (projeções para as telas) | `modelo` |
| `modelo` | entidades JPA, `Situacao` (State), `Cpf`, `Valor`, exceções comuns | nada do projeto |

- **As entidades são o modelo.** Não há record do domínio nem mapper. O pedido de criação já é a entidade (sem id,
  número nem data), passa pelas regras e é gravado.
- **Camadas fechadas para gravar, aberta para ler.**
  - Gravar passa sempre pelo negócio: regras e transação.
  - Ler vai da apresentação direto às projeções de `persistencia.consulta`, sem um serviço que só repassaria. É o
    *architecture sinkhole* que Richards aponta.
- **Interface só com mais de uma implementação real.**
  - `Modalidade`, uma por tipo de processo.
  - Os clientes HTTP, porque o REST Client exige interface.
  - O resto é classe, inclusive o que o negócio chama na integração.
- **Transações** curtas e explícitas no motor, com `QuarkusTransaction`; nos cadastros simples, `@Transactional`.
- **Fronteiras garantidas por teste.** O `ArquiteturaTest` usa o `layeredArchitecture()` do ArchUnit, a *fitness
  function* de Richards para camadas. Garante também que as modalidades e os sistemas externos não se enxergam.

## Consequências

- **Menos arquivos e pacotes.** São 5 camadas e cerca de 15 pacotes, contra 48. Somem ports, mappers, `SegmentoBase`,
  enums espelhados, fakes e o test-jar.
- **Um campo novo passa por três lugares:** a migration, a entidade e o request. Entra também na projeção se a tela
  mostrar o campo.
- **Uma modalidade nova** é uma entidade filha com migration, uma pasta em `negocio` com `Modalidade` e regras, e um
  resource com request.
- **Mais difícil:**
  - o negócio conhece as anotações JPA das entidades;
  - as fronteiras deixam de ser garantidas pelo compilador e passam a ser garantidas por teste (o build quebra do
    mesmo jeito, só que em `mvn test`).
- **O motor deixa de ter teste sem banco.** `ProcessoServiceTest` e `ProcessoRepositoryTest` rodam com o Oracle do
  Dev Services e substituem os fakes e o contrato. As regras, a máquina de estados e os value objects continuam com
  teste unitário puro.
- **Particionamento técnico:** uma mudança de domínio passa pelas camadas. A pasta por modalidade dentro de `negocio`
  limita o espalhamento.
- **A API não mudou.** Os mesmos endpoints, corpos e erros: os ITs passaram sem alteração.
- **Continuam valendo:**
  - o motor único (0002), com o `Segmento` agora chamado `Modalidade`;
  - o State em enum (0006);
  - o compare-and-set (0007);
  - os value objects (0009);
  - o Problem Details (0010);
  - o cadastro sem motor (0012, agora em `negocio/agencia`);
  - o monolito (0013).

## Referências

- RICHARDS, M.; FORD, N. *Fundamentals of Software Architecture*. O'Reilly, 2020 (2ª ed., 2025): estilo em
  camadas, camadas abertas e fechadas, *architecture sinkhole*, *fitness functions*.
- BECK, K. *Tidy First?* O'Reilly, 2023: acoplamento e coesão; cada elemento de estrutura precisa se pagar.
