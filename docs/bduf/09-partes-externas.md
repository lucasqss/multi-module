# 09 — Interações com partes externas

Para cada parte: o que se **envia**, o que se **recebe**, os prazos que importam e o que fazer quando algo dá errado. A forma de troca (manual, arquivo, sistema) não é definida aqui.

## 1. Gestão do grupo (área interna, fonte oficial da cota)

| Envia ao processo | Recebe do processo |
|---|---|
| Contemplação homologada (cota, AGO, forma, valores) | Situação da utilização (em utilização, utilizado) |
| Saldo devedor, % amortizado, prestações em atraso | Pedido de amortização de vincendas (sobra, espécie) |
| Cota quitada, cota excluída, retificações | Troca de titularidade (cessão) |
| Última AGO de contemplação | Crédito pago em espécie / crédito parcial pago |

**Divergência:** a gestão do grupo prevalece; o processo abre um tratamento (UC24).

## 2. Banco da conta do grupo

| Envia | Recebe |
|---|---|
| Ordens de pagamento (beneficiário, conta ou boleto, valor, data, finalidade) | Aceite, confirmação (com identificador), rejeição (com motivo), devolução |
| Consultas de situação | Extrato diário da conta do grupo |

| Situação | Tratamento de negócio |
|---|---|
| Rejeição (dados inválidos, conta encerrada) | Desfaz o comprometimento; corrige os dados com o beneficiário; nova aprovação |
| Sem confirmação no prazo (`PAR-29`) | Consulta a situação **antes** de qualquer reenvio |
| Devolução após o pagamento | Estorno no extrato; contato com o beneficiário; novo pagamento só com nova aprovação |
| Pagamento fora do horário/dia útil | Agendado para o próximo dia útil; o valor segue comprometido |

## 3. Registros

| Órgão | Uso | Envia | Recebe | Atenção |
|---|---|---|---|---|
| Gravame de veículos (SNG) | AF de veículos | Inclusão e baixa de gravame | Confirmação | A anotação no CRV dispensa outro registro (Lei 11.795 art. 14 §7º) |
| Registro de Imóveis | AF de imóvel; averbação de construção | Instrumento para prenotação/registro; pedido de cancelamento | Registro, nota de exigência, cancelamento | Prazo de validade da prenotação; ato único (art. 45) |
| RTD | Contratos de garantia de móveis não veiculares; garantias pessoais quando exigido | Instrumento | Registro | `[A CONFIRMAR]` por tipo de bem |
| Registros específicos (aeronaves, embarcações) | AF desses bens | Instrumento | Registro | `[A CONFIRMAR]` |

**Exigência:** tarefa para a formalização com prazo; perda da prenotação → reinício, com custo e prazo novos.

## 4. CAIXA — FGTS (somente imóvel residencial)

| Envia | Recebe |
|---|---|
| Pedido de uso (lance, complemento, amortização) com documentos do trabalhador e do imóvel | Enquadramento, autorização, liberação ao vendedor (ou à administradora, no lance) |

Requisitos de enquadramento (imóvel residencial urbano, tempo de FGTS, ausência de outro imóvel e de financiamento SFH etc.) e teto de valor do imóvel: sempre os do **manual vigente da CAIXA** `[A CONFIRMAR]` (RN-26). O processo de utilização **aguarda** a liberação do FGTS antes de pagar a parte da carta, a menos que o regulamento preveja outra ordem.

## 5. Seguradora (prestamista)

| Envia | Recebe |
|---|---|
| Aviso de sinistro com documentos | Regulação; indenização ou negativa |

A indenização amortiza ou quita a cota pela gestão do grupo; a sobra vai aos beneficiários (Res. 285 art. 29).

## 6. Avaliação

| Fonte | Uso |
|---|---|
| Tabelas de referência de veículos | Valor de referência de móveis (RP-01) |
| Avaliadores/engenharia | Laudo de imóvel e medição de obra (RP-02, UC08) |

## 7. Credor do financiamento a quitar

| Envia | Recebe |
|---|---|
| Saldo de quitação total com validade | Pagamento |
| Termo de quitação e baixa do gravame/AF | — |

**Atraso no termo ou na baixa:** escalonar dentro da janela (RP-05); a garantia provisória cobre o grupo até regularizar.

## 8. Cobrança / Jurídico (área interna)

| Recebe do processo | Devolve |
|---|---|
| Dossiê de execução: contrato, registro, notificações com prova de recebimento, extrato | Resultado: purgação da mora, valor apurado na venda, saldo remanescente |

## 9. Contabilidade (área interna)

| Recebe | Uso |
|---|---|
| Movimentos dos extratos dos créditos, por grupo e data | Demonstrações do grupo; documentos ao BCB |
| Créditos não procurados | Recursos não procurados |
| Divergências de conciliação | Ajustes |
