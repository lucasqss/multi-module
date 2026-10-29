# 09 — Parametrização do meu sistema

Template para sobrepor ao modelo de referência as características do **seu** sistema e do regulamento dos **seus** grupos. Cada linha aponta para a regra/diagrama que ela altera. Preencha os `TODO`.

## 1. Escopo

| Item | Valor no meu sistema | Afeta |
|---|---|---|
| Segmentos operados (móvel I, móvel II, imóvel, serviço) | `TODO` | 02, PoliticaModalidade |
| Existem grupos com 1ª AGO < 01/07/2024 (`CIRC_3432`)? | `TODO` | 01 §2, RN-03, RN-14, RN-22..24 |
| Finalidades habilitadas (quitação, construção, reforma, planta, terreno, espécie) | `TODO` | RN-09, RN-10 |
| Tipos de lance (livre, fixo, embutido, FGTS) | `TODO` | RN-02, Contemplacao |

## 2. Parâmetros do regulamento (`[CONTRATO]`)

| Parâmetro | Valor | Base |
|---|---|---|
| Prazo de pagamento do lance | `TODO` | art. 2º XIV-a |
| Prazo de análise da documentação | `TODO` | art. 2º XIV-b |
| Índice de atualização do crédito pós-contemplação | `TODO` | art. 2º XVIII-c |
| Garantias aceitas por modalidade | `TODO` | art. 2º XV |
| Procedimento de substituição de garantia | `TODO` | art. 2º XV |
| Quitar saldo com o próprio crédito para espécie? | `TODO` | RP-06 |
| Base dos 10% da sobra com lance embutido (bruto/líquido) | `TODO` | RN-16 `[A CONFIRMAR]` |

## 3. Práticas da administradora (`[PRÁTICA]`)

| Prática | Ligada? | Valor | Regra |
|---|---|---|---|
| Idade máxima de usados | `TODO` | `TODO` | RP-01 |
| Avaliação de imóvel (laudo próprio/terceiro) | `TODO` | `TODO` | RP-02 |
| Retenção final em obra | `TODO` | `TODO %` | RP-03 |
| Prazo máximo da janela sem garantia | `TODO` | `TODO dias` | RP-05 |
| Pagamento só após registro/prenotação | `TODO` | — | RP-07 |

## 4. Integrações existentes

| Integração | Sistema/fornecedor atual | Observação |
|---|---|---|
| Gestão de grupos e assembleias | `TODO` | origem do evento `ContemplacaoHomologada` |
| Pagamentos (SPB/Pix) | `TODO` | |
| Gravame (SNG/B3) | `TODO` | |
| Registro de imóveis (ONR/e-Protocolo) | `TODO` | |
| FGTS (CAIXA) | `TODO` | |
| Workflow/BPM | `TODO` | ver ADR em 08 |

## 5. Regra de elegibilidade do objeto (decisão sua)

O comportamento de `PoliticaModalidade.validarElegibilidade` é o ponto que mais difere entre administradoras. A norma fixa o mínimo (RN-09, RN-10, RN-23); o resto é decisão de produto. Descreva abaixo, em pseudocódigo, as **suas** condições. A ordem de avaliação define qual motivo de recusa o cliente vê primeiro.

```text
validarElegibilidade(objeto, grupo):
  // NORMA (já definido no modelo de referência):
  se objeto.segmento ≠ grupo.segmento                         → recusar "categoria diferente" (RN-09)
  se grupo.subcategoria = II e não objeto.novo                → recusar "subcat. II exige bem novo" (RN-09)
  se grupo.regime = CIRC_3432 e imóvel fora da área de atuação → recusar (RN-23)

  // TODO — suas regras de PRÁTICA (exemplos a decidir):
  //  - usado: idade máxima? limite = min(valor FIPE, valor negociado)?
  //  - imóvel: aceita imóvel rural? comercial? com ônus a quitar no ato?
  //  - serviço: quais tipos de serviço o contrato referencia?
  //  - aceitar mais de um fornecedor por utilização?
  aprovar
```
