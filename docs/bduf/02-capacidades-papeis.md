# 02 — Capacidades, papéis e alçadas

## 1. Mapa de capacidades de negócio

| Capacidade | Sub-capacidades | Casos de uso |
|---|---|---|
| **Gestão do crédito contemplado** | Disponibilizar, aplicar e render; atualizar; manter o extrato; controlar valores comprometidos | UC14, UC01, UC24 |
| **Atendimento à utilização** | Simular; receber a solicitação e a comunicação formal; orientar; gerir pendências | UC01, UC02, UC03 |
| **Qualificação de partes** | Qualificar o beneficiário; qualificar o garantidor; qualificar o cessionário | UC23, UC04, UC17 |
| **Análise da utilização** | Capacidade de pagamento; PLD; elegibilidade e avaliação do objeto | UC04, UC05 |
| **Formalização e garantias** | Instrumentos; assinatura; registro; substituição; baixa; disparo da execução | UC06, UC16, UC19, UC18 |
| **Pagamentos** | Pré-condições; à vista; por etapas; adiantamento; quitação; sinal; sobra; espécie; crédito parcial | UC13, UC07–UC12, UC15, UC22 |
| **Eventos da cota** | Cessão; sinistro; exclusão; encerramento do grupo | UC17, UC21, UC22, UC20 |
| **Controle e conciliação** | Conciliar o extrato do crédito com a conta do grupo e com a gestão do grupo; tratar divergências | UC24 |
| **Governança de regras** | Manter regulamentos, parâmetros, políticas por modalidade e calendário; registrar a vigência | [11](11-parametros-motivos.md) |

## 2. Papéis

| Papel | Descrição |
|---|---|
| **Consorciado** | Titular da cota contemplada (ou representante legal/procurador) |
| **Beneficiário** | Vendedor, fornecedor, prestador, construtora ou credor que recebe o pagamento |
| **Garantidor** | Fiador, avalista ou proprietário de bem dado em garantia |
| **Atendimento** | Orienta, registra solicitações em nome do cliente (com evidência), gere pendências |
| **Analista de crédito** | Decide sobre a capacidade de pagamento e a garantia complementar |
| **Analista de PLD** | Qualifica partes, trata alertas, decide sobre risco alto |
| **Analista de formalização** | Analisa o objeto e os documentos; constitui, acompanha e substitui garantias |
| **Engenharia / vistoria** | Avalia imóveis; mede etapas de obra/serviço |
| **Preparador de pagamento** | Monta a ordem de pagamento a partir de uma solicitação apta |
| **Aprovador de pagamento** | Aprova ordens dentro da sua alçada; não pode ser o preparador |
| **Tesouraria** | Disponibiliza o crédito; acompanha a liquidação; concilia |
| **Cobrança / Jurídico** | Recebe o dossiê de execução e devolve o resultado |
| **Gestor de regras** | Mantém parâmetros, políticas e calendário, com aprovação de Compliance para o que for `[NORMA]` |

## 3. Matriz RACI (R = executa, A = responde, C = consultado, I = informado)

| Atividade | Consorc. | Atend. | Crédito | PLD | Formaliz. | Engenh. | Prep. pag. | Aprov. pag. | Tesour. | Jurídico |
|---|---|---|---|---|---|---|---|---|---|---|
| Disponibilizar crédito (UC14) | I | I | | | | | | | **R/A** | |
| Solicitar e comunicar formalmente (UC02) | **R** | C | | | I | | | | | |
| Gerir documentos (UC03) | R | **A** | | | R | | | | | |
| Qualificar beneficiário (UC23) | C | R | | **A** | | | | | | |
| Capacidade de pagamento (UC04) | C | | **R/A** | C | | | | | | |
| Análise do objeto (UC05) | C | | | | **R/A** | R | | | | |
| Garantia (UC06/16) | R (assina) | | C | | **R/A** | | | | | C |
| Pré-condições e ordem (UC13/07) | | | | | C | | **R** | **A** | I | |
| Medição de etapa (UC08) | R (autoriza) | | | | C | **R/A** | | | | |
| Quitação de financiamento (UC09) | C | | | | **A** | | R | R | R | |
| Destinação da sobra (UC10) | **R** | C | | | | | R | A | | |
| Espécie / crédito parcial (UC15/22) | R | C | | C | | | R | **A** | R | |
| Cessão (UC17) | R | R | R | R | **A** | | | | | C |
| Sinistro (UC21) | R (espólio) | **A** | | | R | | | | R | C |
| Execução de garantia (UC18) | I | | | | R | | | | | **A** |
| Baixa de garantia (UC19) | I | | | | **R/A** | | | | | |
| Conciliação (UC24) | | | | | | | | | **R/A** | |

## 4. Alçadas e segregação de funções

| Controle | Regra de negócio |
|---|---|
| Quatro olhos | Toda ordem de pagamento tem preparador e aprovador distintos |
| Alçada por valor | Faixas definidas em `PAR-30` (ex.: até X, analista sênior; acima de Y, gerência) |
| Alçada por finalidade | Espécie, devolução de sobra, crédito parcial e pagamento ao próprio consorciado exigem aprovador de nível superior `[PRÁTICA]` |
| Conflito de interesse | Quem analisou a capacidade não aprova o pagamento da mesma solicitação `[PRÁTICA]` |
| Alteração de dados bancários | A troca de conta de beneficiário após a aprovação da ordem invalida a ordem e exige nova aprovação |
| Exceções a regras `[PRÁTICA]` | Permitidas com justificativa e aprovação de alçada; regras `[NORMA]` não admitem exceção |
| Ajustes no saldo | Qualquer ajuste manual no extrato do crédito exige dupla aprovação e justificativa |
