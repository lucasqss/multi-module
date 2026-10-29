# 14 — Rastreabilidade

Processos BPMN em `../utilizacao-credito/diagramas/bpmn/`: **P1** = 01 principal · **P2** = 02 etapas · **P3** = 03 quitação · **P4** = 04 eventos pós-contemplação · **P5** = 05 cessão, sinistro e crédito parcial.

| UC | Nome | Regras | Processo | Ciclo de vida | Fatos | Motivos | Critérios |
|---|---|---|---|---|---|---|---|
| UC01 | Consultar e simular | RN-04, RN-13, RN-16 | P1 | Crédito | — | — | CA-08 |
| UC02 | Indicar finalidade (comunicação formal) | RN-07, RN-09, RN-10, RN-17, RN-22, RN-23 | P1 | Solicitação | FN-04 | MOT-010..015, MOT-090 | CA-05, CA-06, CA-07, CA-15, CA-35, CA-37 |
| UC03 | Documentação | RN-17 | P1 | Solicitação | FN-05, FN-06 | MOT-030, MOT-031 | CA-40 |
| UC04 | Capacidade e PLD | RN-05, RN-19 | P1 | Solicitação | FN-08 | — | CA-39 |
| UC05 | Análise do objeto | RN-09, RN-10, RN-23, RP-01, RP-02 | P1 | Solicitação | FN-08 | — | CA-08 |
| UC06 | Constituir/registrar garantia | RN-07, RN-11 | P1, P3 | Garantia | FN-10, FN-11 | — | CA-10, CA-36 |
| UC07 | Pagar à vista | RN-06, RN-07, RN-18, RN-22 | P1 | Pagamento | FN-12, FN-13, FN-14 | MOT-054 | CA-09, CA-11..CA-14 |
| UC08 | Pagar por etapas | RN-06, RN-08, RP-03 | P2 | Pagamento, Etapa | FN-15 | MOT-053 | CA-24, CA-25 |
| UC09 | Quitar financiamento | RN-07, RN-10, RP-05 | P3 | Solicitação, Garantia | FN-16 | MOT-020..023 | CA-07, CA-22, CA-23 |
| UC10 | Destinar sobra | RN-16 | P1 | Solicitação | FN-17 | MOT-060..062 | CA-16..CA-19 |
| UC11 | Complementar | RN-19, RN-26 | P1 | Solicitação | — | — | — |
| UC12 | Ressarcir sinal | RN-22 | P1 | Pagamento | FN-13 | MOT-090 | CA-15 |
| UC13 | Pré-condições de pagamento | RN-06, RN-07, RN-08, RN-18 | P1, P2, P3 | Pagamento | — | MOT-030, 040, 050..056 | CA-09, CA-10 |
| UC14 | Disponibilizar | RN-01..RN-04, RN-20 | P1 | Crédito | FN-01..FN-03 | — | CA-01..CA-04, CA-38 |
| UC15 | Espécie | RN-13, RN-18, RP-06 | P4 | Crédito | FN-19, FN-20 | MOT-052, MOT-070, MOT-071 | CA-20, CA-21, CA-34 |
| UC16 | Substituir garantia | art. 2º XV | P2 | Garantia | FN-28 | MOT-042 | CA-25 |
| UC17 | Ceder cota | Lei art. 13; RN-05, RN-19 | P5 | Cessão | FN-22 | MOT-081 | CA-31 |
| UC18 | Executar garantia | RN-15 | P4 | Garantia | FN-24 | — | CA-27 |
| UC19 | Baixar garantia | RN-12 | P4 | Garantia | FN-25 | MOT-041 | CA-26 |
| UC20 | Encerramento | RN-25 | P4 | Crédito | FN-26 | — | CA-33 |
| UC21 | Óbito/sinistro | arts. 14, 29 | P5 | Sinistro, Crédito | FN-23 | — | CA-32 |
| UC22 | Crédito parcial | RN-14, RN-24 | P4, P5 | Crédito | FN-21 | MOT-080 | CA-28..CA-30 |
| UC23 | Qualificar beneficiário | RN-07, RN-19 | P1 | — | FN-07 | MOT-055 | — |
| UC24 | Conciliar | — | — | — | FN-27 | — | — |

## Cobertura de regras por critério de aceite

| Regra | Critérios | | Regra | Critérios |
|---|---|---|---|---|
| RN-02 | CA-03 | | RN-13 | CA-20, CA-21 |
| RN-03 | CA-01, CA-02 | | RN-14 | CA-28 |
| RN-06 | CA-09 | | RN-15 | CA-27 |
| RN-07 | CA-10 | | RN-16 | CA-16..CA-19 |
| RN-08 | CA-24 | | RN-22 | CA-15 |
| RN-09 | CA-05, CA-06 | | RN-24 | CA-29, CA-30 |
| RN-10 | CA-07, CA-22 | | RN-25 | CA-33 |
| RN-12 | CA-26 | | RP-05 | CA-23 |
| RN-01 | CA-37 | | RN-17 | CA-40 |
| RN-04 | CA-38 | | RN-18 | CA-34 |
| RN-05 | CA-39 | | RN-23 | CA-35 |
| RN-11 | CA-36 | | | |

Regras **sem critério de aceite ainda**: RN-19 (PLD), RN-20 (atualização) e RN-26 (FGTS). Elas dependem de definições externas: a política de PLD, o índice do regulamento (PAR-03) e o manual da CAIXA. Os critérios devem ser escritos quando essas definições existirem.
