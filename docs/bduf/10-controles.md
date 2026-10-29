# 10 — Controles e compliance

## 1. Controles-chave do processo

| ID | Controle | Tipo | Risco mitigado | Regra |
|---|---|---|---|---|
| CT-01 | Pagamento só a beneficiário permitido e qualificado | Preventivo | Desvio de recursos do grupo | RN-06, RN-18 |
| CT-02 | Quatro olhos e alçada em todo pagamento | Preventivo | Fraude interna, erro | [02 §4](02-capacidades-papeis.md) |
| CT-03 | Pré-condições do art. 18 §2º verificadas antes de cada pagamento | Preventivo | Pagamento sem garantia ou sem documento | RN-07 |
| CT-04 | Elegibilidade do objeto por categoria e subcategoria | Preventivo | Uso indevido do crédito | RN-09 |
| CT-05 | Garantia registrada antes do pagamento (salvo RP-07) | Preventivo | Grupo descoberto | RN-11 |
| CT-06 | Janela sem garantia com prazo e escalonamento | Detectivo | Grupo descoberto na quitação | RP-05 |
| CT-07 | Garantia só é baixada com a cota quitada | Preventivo | Liberação indevida (dever de indenizar o grupo) | RN-12 |
| CT-08 | Prazo D+3 monitorado | Detectivo | Descumprimento normativo | RN-03 |
| CT-09 | Teto de 10% e destinações da sobra | Preventivo | Uso livre disfarçado | RN-16 |
| CT-10 | 180 dias + quitação para espécie | Preventivo | Liberação antecipada em dinheiro | RN-13 |
| CT-11 | Conciliação diária | Detectivo | Pagamento duplicado, perdido ou divergente | UC24 |
| CT-12 | Troca de conta de beneficiário invalida a aprovação | Preventivo | Fraude de troca de conta | [02 §4](02-capacidades-papeis.md) |
| CT-13 | Evidência de capacidade guardada | Evidência | Fiscalização BCB | RN-05 |
| CT-14 | Regras versionadas e registradas em cada decisão | Evidência | Impossibilidade de explicar decisões passadas | RC-1 |
| CT-15 | Revisão periódica de acessos e perfis | Preventivo | Acesso indevido | — |

## 2. PLD/FT (Circ. BCB 3.978/2020)

**Situações a monitorar:**
- pagamento a beneficiário PF sem relação aparente com o objeto;
- vários pagamentos fracionados ao mesmo beneficiário;
- complemento alto com recursos próprios incompatíveis com a renda;
- "aquisição simbólica" seguida de pedido de devolução da sobra;
- cessão de cota contemplada com indícios de comercialização irregular;
- quitação de financiamento de bem incompatível com o perfil;
- espécie logo após 180 dias para consorciado de alto risco.

**Tratamento:** o alerta vai à fila de PLD; o pagamento fica retido enquanto a análise estiver aberta, se a política assim definir; a decisão é fundamentada; a comunicação ao COAF segue o procedimento interno (sigiloso, sem informar o cliente).

## 3. LGPD

| Tema | Diretriz |
|---|---|
| Bases legais | Execução de contrato (utilização); obrigação legal/regulatória (BCB, PLD); exercício regular de direitos (garantias, execução) |
| Minimização | Coletar só o que [06](06-modelo-informacao.md) e [07](07-documentos.md) exigem para a finalidade escolhida |
| Dados de terceiros | Beneficiários, garantidores, cessionários e sucessores: informar a finalidade e a base legal |
| Acesso | Por papel; o consorciado vê só as próprias cotas; o beneficiário vê só o que lhe diz respeito |
| Direitos do titular | Atender sem apagar o que a norma obriga a guardar (§5) |
| Compartilhamento | Registros, banco, CAIXA, seguradora: só o necessário para cada finalidade |

## 4. Sigilo

As informações de cotas, saldos e pagamentos estão sob sigilo das operações (LC 105/2001) `[A CONFIRMAR enquadramento com o Jurídico]`. Divulgação a terceiros só nas hipóteses legais.

## 5. Guarda de informações

| Informação | Prazo mínimo | Base |
|---|---|---|
| Evidência de capacidade de pagamento (contemplação, cessão) | 5 anos após o encerramento do grupo | Res. 285 art. 3º §3º |
| Dossiê da utilização (documentos, decisões, garantias, pagamentos) | 5 anos após o encerramento do grupo `[PRÁTICA]` (prescrição de 5 anos — Lei 11.795 art. 32 §2º) | Jurídico |
| Registros de PLD | Conforme Circ. 3.978 | PLD |
| Comunicações com controle de recebimento | Igual ao dossiê | — |

## 6. Trilha de auditoria (o que precisa ser possível responder)

1. Quem decidiu, quando, com quais documentos e **com qual regra vigente**?
2. Por que esse beneficiário recebeu esse valor nessa data?
3. Como o saldo chegou a esse número? (extrato completo)
4. Qual era a garantia do grupo em cada dia?
5. O cliente foi comunicado? Recebeu?
6. Houve exceção? Quem autorizou e por quê?
