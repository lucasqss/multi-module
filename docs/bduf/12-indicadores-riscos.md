# 12 — Indicadores, SLAs e riscos

## 1. Indicadores

| ID | Indicador | Fórmula | Uso |
|---|---|---|---|
| IN-01 | Cumprimento do D+3 | créditos disponibilizados no prazo ÷ total | Compliance |
| IN-02 | *Lead time* de utilização | da comunicação formal ao 1º pagamento confirmado (mediana, p90), por modalidade | Cliente / operação |
| IN-03 | Tempo em pendência | soma dos períodos em `PENDENTE_DOCUMENTOS` | Qualidade do checklist |
| IN-04 | Taxa de 1ª passagem | solicitações aprovadas sem nenhuma pendência ÷ total | Clareza das orientações |
| IN-05 | Taxa de reprovação por motivo | reprovações por `MOT` | Política e comunicação |
| IN-06 | Cumprimento do prazo de análise (`PAR-06`) | análises no prazo ÷ total | Contrato |
| IN-07 | Tempo de registro de garantia | da assinatura ao registro, por órgão | Parceiros |
| IN-08 | Janelas sem garantia abertas / estouradas | quantidade e idade | Risco |
| IN-09 | Pagamentos rejeitados/devolvidos | ÷ total de pagamentos | Qualidade cadastral |
| IN-10 | Divergências de conciliação | abertas e idade | Controle |
| IN-11 | Créditos parados | créditos `DISPONIBILIZADO` há > N dias | Ação comercial |
| IN-12 | Sobras não destinadas | quantidade e valor | Cliente |
| IN-13 | Alertas de PLD | abertos, tempo de tratamento | Compliance |
| IN-14 | Exceções a regras `[PRÁTICA]` | quantidade por regra e alçada | Governança |

## 2. Prazos de serviço (SLA)

| Etapa | Prazo | Origem |
|---|---|---|
| Disponibilização | D+3 úteis | `[NORMA]` |
| Análise de documentos | `PAR-06` | `[CONTRATO]` |
| Qualificação de beneficiário | definir (ex.: 1 dia útil) | `[PRÁTICA]` |
| Aprovação de pagamento | definir (ex.: mesmo dia, se até X horas) | `[PRÁTICA]` |
| Pagamento ao fornecedor | "compatível com a venda à vista ou na forma acordada" (art. 18) | `[NORMA]` |
| Janela sem garantia | `PAR-25` | `[PRÁTICA]` |
| Comunicação no encerramento | ≤ 60 dias após a última AGO de contemplação | `[NORMA]` |
| Pagamento de recurso não procurado | ≤ 30 dias do comparecimento | `[NORMA]` |

## 3. Riscos de negócio

| ID | Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|---|
| RK-01 | Regra aplicada sem distinguir o regime (`RES_285` × `CIRC_3432`) | Média | Alto | Regime como dado do grupo; critérios de aceite por regime ([13](13-criterios-aceite.md)) |
| RK-02 | Regulamentos sem os itens que a norma delega | Alta | Alto | Levantamento prévio por grupo; Jurídico define os ausentes |
| RK-03 | Pagamento em duplicidade | Baixa | Alto | RC-2, comprometimento do saldo, conciliação (CT-11) |
| RK-04 | Fraude de troca de conta do beneficiário | Média | Alto | CT-12; confirmação da titularidade |
| RK-05 | Grupo descoberto na quitação de financiamento | Média | Alto | CT-06; garantia provisória |
| RK-06 | Obra parada após pagamentos parciais | Média | Alto | Medição, retenção, garantia provisória, troca de fornecedor |
| RK-07 | Adiantamento sem contraprestação | Baixa | Alto | RN-08 (responsabilidade da administradora); preferir pagar contra medição |
| RK-08 | Comercialização irregular de cota contemplada | Média | Médio | Análise de cessão; PLD |
| RK-09 | Atraso de cartórios/registros | Alta | Médio | Indicador IN-07; pagamento contra prenotação, se a política permitir (PAR-13) |
| RK-10 | Pontos `[A CONFIRMAR]` decididos tarde | Média | Médio | Lista em [`../utilizacao-credito/10-referencias-e-pendencias.md`](../utilizacao-credito/10-referencias-e-pendencias.md); decisão jurídica antes da implantação |
| RK-11 | Mudança normativa | Média | Médio | Regras versionadas; vigência por data |
| RK-12 | Liberação de garantia antes da quitação | Baixa | Alto | CT-07 |

## 4. Decisões pendentes (negócio/jurídico)

| # | Decisão | Afeta |
|---|---|---|
| D-01 | Base do teto de 10% (PAR-01) | UC10 |
| D-02 | Quitação de financiamento conta como "utilização para aquisição" na vedação de exclusão? | UC22, RN-14 |
| D-03 | Atualização e rendimento coexistem sobre a mesma base? | UC14, 05 §4 |
| D-04 | Pagar contra prenotação ou só após o registro (PAR-13)? | UC07 |
| D-05 | Retenção do pagamento durante alerta de PLD? | 10 §2 |
| D-06 | Permitir quitar a cota com o próprio crédito para a espécie (PAR-27)? | UC15 |
| D-07 | Órgão de registro por tipo de bem móvel não veicular | UC06 |
