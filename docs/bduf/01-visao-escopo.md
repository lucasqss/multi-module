# 01 — Visão, escopo e glossário

## 1. Visão

Um **processo único de utilização do crédito** que vale para todas as modalidades. Ele começa na *contemplação homologada* e termina com o crédito **utilizado, garantido e conciliado**, e mais tarde com a garantia **baixada**. O processo combina:

- um **tronco comum** de etapas (disponibilizar → escolher → analisar → garantir → pagar → destinar sobra);
- **políticas por modalidade** que variam só em cinco pontos: elegibilidade do objeto, documentos, garantia, avaliação e forma de pagamento ([`../utilizacao-credito/02-modalidades.md`](../utilizacao-credito/02-modalidades.md));
- **regras por regime normativo e por regulamento de grupo**, nunca uma regra única para todos os grupos;
- um **extrato do crédito**, em que todo movimento de valor (rendimento, pagamento, devolução) fica registrado e explica o saldo;
- **evidências** de cada decisão, pagamento e comunicação.

## 2. Objetivos de negócio (calibrar)

| Objetivo | Indicador | Meta sugerida |
|---|---|---|
| Cumprir o prazo de disponibilização | % de créditos disponíveis até D+3 úteis | 100% |
| Agilizar a utilização | Mediana entre a comunicação formal e o pagamento | ≤ 10 dias úteis (móvel); ≤ 30 (imóvel) |
| Pagar somente a quem a norma permite | Pagamentos fora das hipóteses de RN-06/RN-18 | 0 |
| Não deixar o grupo descoberto | Quitações com janela sem garantia acima do prazo | 0 sem tratamento |
| Saldo sempre explicável | Divergências não tratadas entre o extrato do crédito e a conta do grupo | 0 |
| Transparência para o cliente | % de pendências comunicadas com motivo e ação esperada | 100% |

## 3. Escopo

### Dentro
- Os 24 casos de uso de [03](03-casos-de-uso.md): consulta e simulação; solicitação e comunicação formal; documentos; análises de capacidade, PLD e objeto; garantias (constituição, registro, substituição, baixa e disparo da execução); pagamentos (à vista, por etapas, adiantamento, quitação, sobra, sinal, espécie, crédito parcial); cessão; sinistro; encerramento; conciliação.
- Qualificação dos **beneficiários** dos pagamentos (vendedores, fornecedores, prestadores, credores).
- Relacionamento com consorciado, fornecedor e garantidor durante a utilização.

### Fora (o processo consome o resultado, não executa)
| Capacidade | Área responsável | O que o processo recebe/entrega |
|---|---|---|
| Grupos, cotas, assembleias, contemplação, prestações e saldo devedor | Gestão do grupo | Recebe a contemplação, o saldo devedor, a quitação e a exclusão; entrega amortizações e a situação da utilização |
| Cobrança e contencioso (a execução em si) | Cobrança/Jurídico | Entrega o dossiê; recebe o resultado |
| Contabilidade do grupo e da administradora, documentos ao BCB | Contabilidade | Entrega os movimentos do crédito |
| Aplicação financeira dos recursos do grupo | Tesouraria | Recebe o rendimento do grupo |
| Seguro prestamista | Seguradora | Recebe a indenização |

## 4. Partes interessadas

Diagrama: [`diagramas/01-partes-envolvidas.puml`](diagramas/01-partes-envolvidas.puml)

| Parte | Interesse |
|---|---|
| Consorciado | Rapidez; saber o saldo, o que falta e quando recebe |
| Fornecedor / credor | Receber com previsibilidade e regras claras |
| Garantidor | Saber o que garante e quando é liberado |
| Operação (atendimento, crédito, formalização, tesouraria) | Saber o que fazer, em que ordem e com que prazo |
| Compliance / PLD | Alertas tempestivos, evidências, comunicação ao COAF |
| Jurídico | Garantias válidas; prova de comunicação |
| Auditoria / BCB | Rastrear regra → decisão → pagamento |
| Contabilidade | Movimentos conciliáveis por grupo |

## 5. Premissas de negócio

1. Os recursos do grupo são **segregados** dos da administradora; todo pagamento sai dos recursos do grupo.
2. A **gestão do grupo** é a fonte oficial da cota (situação, saldo devedor, percentual amortizado). O processo de utilização não recalcula prestações.
3. O **regulamento** de cada grupo traz os itens que a norma delega ao contrato (prazos, garantias, índice de atualização, documentos). Os itens que faltarem precisam ser definidos antes da operação (risco RK-02).
4. Há um **calendário de feriados** nacionais, estaduais e municipais da localidade de constituição de cada grupo.
5. Normas consideradas: textos consolidados até setembro/2026.

## 6. Glossário

| Termo | Definição |
|---|---|
| **Carta de crédito / crédito** | Valor do bem/serviço de referência na AGO de contemplação, disponível ao contemplado |
| **Contemplação** | Atribuição, em AGO, do direito de usar o crédito, por sorteio ou lance |
| **Homologação** | Confirmação da contemplação; no lance, só após o recebimento (Res. 285 art. 12 II) |
| **Lance embutido** | Parte do lance paga com o próprio crédito; reduz o valor disponível |
| **Saldo utilizável** | Quanto o consorciado ainda pode usar: crédito disponível + rendimentos + atualização − pagamentos e valores comprometidos |
| **Valor comprometido** | Parte do saldo separada para um pagamento autorizado que ainda não foi confirmado |
| **Extrato do crédito** | Lista cronológica e imutável de todos os movimentos que formam o saldo |
| **Solicitação de utilização** | Pedido do consorciado para usar o crédito numa finalidade |
| **Comunicação formal** | Manifestação do consorciado com identificação completa do objeto, do beneficiário e das condições (art. 18 §2º I); marca o início dos prazos de análise |
| **Beneficiário** | Quem recebe o pagamento: vendedor, fornecedor, prestador, construtora, credor ou, nas exceções, o próprio consorciado |
| **Objeto** | O que o crédito paga: bem móvel, imóvel, serviço ou financiamento a quitar |
| **Sobra** | Saldo que resta após a aquisição; tem três destinações (art. 18 §3º) |
| **Complemento** | Valor do objeto acima do crédito, pago com recursos próprios ou FGTS |
| **Espécie** | Recebimento do crédito em dinheiro, com 180 dias da contemplação e quitação |
| **Crédito parcial** | Valor devido ao contemplado excluído que não usou o crédito (art. 33) |
| **Janela sem garantia** | Intervalo entre o pagamento da quitação de um financiamento e o registro da nova garantia a favor do grupo |
| **Garantia provisória** | Garantia sem vínculo ao bem, usada enquanto o bem não existe ou quando o objeto é serviço |
| **Quatro olhos** | Pagamento preparado por uma pessoa e aprovado por outra |
| **Regime normativo** | `RES_285` ou `CIRC_3432`, conforme a data da 1ª AGO do grupo |
| **SNG / RI / RTD** | Sistema Nacional de Gravames / Registro de Imóveis / Registro de Títulos e Documentos |
