# BDUF negocial — Pagamento da carta de crédito de consórcio

Definição **de negócio**, completa e antecipada (*Big Design Up Front*), do processo de **utilização do crédito pós-contemplação** em todas as modalidades: bens móveis (subcategorias I e II), imóveis (pronto, planta, terreno, construção, reforma, empreendimento), serviços e as finalidades transversais (quitação de financiamento, espécie, crédito parcial de excluído, sobra, ressarcimento de sinal).

**Agnóstico de arquitetura, tecnologia e forma de desenvolvimento.** O documento diz **o que** precisa acontecer, **quem** faz, **quando**, **com qual informação** e **sob qual regra**. **Como** construir fica com o time de solução, que pode usar qualquer sistema, plataforma ou processo manual.

Base normativa, modalidades e processos de referência: [`../utilizacao-credito/`](../utilizacao-credito/README.md), em especial o [catálogo de regras](../utilizacao-credito/07-regras-de-negocio.md) (`RN-xx`, `RP-xx`) e os [BPMN](../utilizacao-credito/diagramas/bpmn/).

## Índice

| # | Documento | Responde |
|---|---|---|
| 01 | [Visão, escopo e glossário](01-visao-escopo.md) | Para que serve, até onde vai, vocabulário |
| 02 | [Capacidades, papéis e alçadas](02-capacidades-papeis.md) | Quem faz o quê; segregação de funções |
| 03 | [Casos de uso](03-casos-de-uso.md) | UC01–UC24: fluxos, alternativas, exceções |
| 04 | [Ciclos de vida](04-ciclos-de-vida.md) | Situações possíveis e transições permitidas |
| 05 | [Cálculos](05-calculos.md) | Saldo, rendimentos, sobra, complemento, crédito parcial, prazos |
| 06 | [Modelo de informação](06-modelo-informacao.md) | Informações de negócio, obrigatoriedade, dono, guarda |
| 07 | [Documentos por finalidade](07-documentos.md) | Checklists por modalidade e responsável |
| 08 | [Fatos de negócio e comunicações](08-fatos-comunicacoes.md) | O que acontece, quem precisa saber, como comprovar |
| 09 | [Interações com partes externas](09-partes-externas.md) | O que se troca com banco, registros, CAIXA, seguradora |
| 10 | [Controles e compliance](10-controles.md) | PLD, LGPD, evidências, segregação, guarda |
| 11 | [Parâmetros, políticas e motivos](11-parametros-motivos.md) | O que é configurável; catálogo de motivos de recusa/pendência |
| 12 | [Indicadores, SLAs e riscos](12-indicadores-riscos.md) | Como medir; o que pode dar errado |
| 13 | [Critérios de aceite](13-criterios-aceite.md) | Cenários Dado/Quando/Então por regra e UC |
| 14 | [Rastreabilidade](14-rastreabilidade.md) | UC × regra × processo × ciclo de vida × critério |

### Diagramas (`diagramas/`)
`01-partes-envolvidas.puml` · `02-ciclo-solicitacao.puml` · `03-ciclo-garantia.puml` · `04-ciclo-pagamento.puml` · BPMN complementar em [`../utilizacao-credito/diagramas/bpmn/05-cessao-sinistro-credito-parcial.bpmn`](../utilizacao-credito/diagramas/bpmn/05-cessao-sinistro-credito-parcial.bpmn)

## Convenções

| Convenção | Significado |
|---|---|
| `[NORMA]` / `[CONTRATO]` / `[PRÁTICA]` / `[A CONFIRMAR]` | Origem da regra (ver referência) |
| `RES_285` / `CIRC_3432` | Regime normativo do grupo (1ª AGO a partir de 01/07/2024 / anterior) |
| `UCxx` | Caso de uso |
| `MOT-xxx` | Motivo de recusa ou pendência comunicado ao cliente ([11](11-parametros-motivos.md)) |
| `FN-xx` | Fato de negócio ([08](08-fatos-comunicacoes.md)) |
| `PAR-xx` | Parâmetro de negócio ([11](11-parametros-motivos.md)) |
| `CA-xx` | Critério de aceite ([13](13-criterios-aceite.md)) |
