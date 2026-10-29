# Utilização da carta de crédito de consórcio — Documentação de referência

Esta pasta documenta o processo **pós-contemplação**: da homologação da contemplação até o crédito utilizado, a garantia registrada e, por fim, baixada. Cobre as modalidades **bens móveis** (subcategorias I e II), **bens imóveis** (pronto, planta, terreno, construção, reforma, empreendimento), **serviços** e as finalidades que valem para todas elas: **quitação de financiamento**, **crédito em espécie** e **crédito parcial de excluído**.

O objetivo é servir de **modelo de referência genérico**. A solução real aplica por cima dele as características do sistema e do regulamento de cada administradora, a partir do documento [09 — Parametrização do meu sistema](09-parametrizacao-do-meu-sistema.md).

## Como ler

| # | Documento | Diagramas |
|---|---|---|
| 01 | [Contexto regulatório e regime normativo](01-contexto-regulatorio.md) | — |
| 02 | [Modalidades e suas divergências](02-modalidades.md) | — |
| 03 | [Casos de uso](03-casos-de-uso.md) | `puml/01-casos-de-uso.puml` |
| 04 | [Modelo de domínio](04-modelo-dominio.md) | `puml/02-modelo-dominio.puml`, `puml/03-maquina-estados-credito.puml` |
| 05 | [Processos (BPMN)](05-processos-bpmn.md) | `bpmn/01..04-*.bpmn` |
| 06 | [Jornada do cliente](06-jornada-cliente.md) | `puml/04-jornada-cliente.puml`, `puml/05..07-sequencia-*.puml` |
| 07 | [Catálogo de regras de negócio](07-regras-de-negocio.md) | — |
| 08 | [Arquitetura da solução unificada](08-arquitetura-solucao-unificada.md) | `puml/08-componentes-solucao-unificada.puml` |
| 09 | [Parametrização do meu sistema](09-parametrizacao-do-meu-sistema.md) | — (template a preencher) |
| 10 | [Referências e pontos em aberto](10-referencias-e-pendencias.md) | — |

## Convenção de rótulos

Toda regra recebe um rótulo que diz de onde ela vem:

| Rótulo | Significado | Implicação para o sistema |
|---|---|---|
| `[NORMA]` | Lei 11.795/2008, Res. BCB 285/2023, Circ. BCB 3.432/2009 etc. (artigo citado) | Bloqueio obrigatório, versionado por regime |
| `[CONTRATO]` | A norma delega ao regulamento do grupo (Res. 285 art. 2º) | Parâmetro lido do `RegulamentoGrupo` (não fixo no código) |
| `[PRÁTICA]` | Política de mercado/da administradora, sem força normativa | Configurável e desligável; nunca apresentada como “exigência regulatória” |
| `[A CONFIRMAR]` | Ponto não confirmado em fonte primária | Validar com jurídico antes de implementar |

## Visualização dos diagramas

- **PlantUML (`.puml`)**: plugin PlantUML do IntelliJ/VS Code, ou `plantuml -tsvg diagramas/puml/*.puml`.
- **BPMN (`.bpmn`)**: BPMN 2.0 com DI (layout), abre no **Camunda Modeler**, em [demo.bpmn.io](https://demo.bpmn.io) ou no plugin BPMN do IntelliJ. Cada tarefa traz a base legal no campo *Documentation*.

> Não é aconselhamento jurídico. A base normativa reflete os textos consolidados conferidos até **setembro/2026**.
