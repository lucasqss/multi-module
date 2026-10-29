# 04 — Ciclos de vida

Situações em que cada elemento do negócio pode estar e as passagens permitidas. **Qualquer passagem não listada é proibida.** Os nomes das situações são os mesmos dos diagramas em `diagramas/`.

## 1. Crédito contemplado

| Situação | Significado |
|---|---|
| `PENDENTE_HOMOLOGACAO` | Contemplado por lance, aguardando o pagamento do lance |
| `DISPONIBILIZADO` | Crédito à disposição, aplicado e rendendo; nenhuma solicitação em curso |
| `EM_UTILIZACAO` | Há solicitação em curso; nenhum pagamento de aquisição confirmado |
| `UTILIZADO` | Ao menos um pagamento de aquisição confirmado (pode haver saldo para sobra ou etapas) |
| `PAGO_EM_ESPECIE` | Crédito pago em dinheiro ao consorciado |
| `CREDITO_PARCIAL` | Cota excluída; crédito parcial apurado/pago |
| `CONTEMPLACAO_CANCELADA` | Contemplação cancelada em AGO (apenas `CIRC_3432`) |
| `NAO_PROCURADO` | Grupo encerrado sem que o crédito fosse usado ou reclamado |
| `SUSPENSO` | Bloqueado por óbito, ordem judicial ou fraude sob apuração |
| `CANCELADO` | Lance não pago; a contemplação não se confirmou |

| De | Para | Quando | Condição |
|---|---|---|---|
| — | `PENDENTE_HOMOLOGACAO` | contemplação por lance | — |
| — / `PENDENTE_HOMOLOGACAO` | `DISPONIBILIZADO` | homologação | RN-01, RN-02 |
| `PENDENTE_HOMOLOGACAO` | `CANCELADO` | lance não pago no prazo | — |
| `DISPONIBILIZADO` | `EM_UTILIZACAO` | 1ª solicitação confirmada | — |
| `EM_UTILIZACAO` | `DISPONIBILIZADO` | solicitações encerradas sem pagamento | — |
| `EM_UTILIZACAO` | `UTILIZADO` | 1º pagamento de aquisição confirmado | daqui em diante: RN-14 (sem exclusão) e RN-15 (execução) |
| `DISPONIBILIZADO` / `EM_UTILIZACAO` | `PAGO_EM_ESPECIE` | espécie paga | RN-13 |
| `DISPONIBILIZADO` / `EM_UTILIZACAO` | `CREDITO_PARCIAL` | cota excluída | `RES_285` e sem aquisição |
| `DISPONIBILIZADO` / `EM_UTILIZACAO` | `CONTEMPLACAO_CANCELADA` | deliberação da AGO | `CIRC_3432` |
| `DISPONIBILIZADO` / `EM_UTILIZACAO` | `NAO_PROCURADO` | encerramento do grupo | RN-25 |
| qualquer não final | `SUSPENSO` | óbito, ordem judicial, fraude | — |
| `SUSPENSO` | situação anterior | causa resolvida | — |

A **elegibilidade à espécie** não é uma situação: vale a partir de 180 dias da contemplação. `UTILIZADO` ainda aceita novas solicitações para o saldo remanescente.

## 2. Solicitação de utilização

Diagrama: [`diagramas/02-ciclo-solicitacao.puml`](diagramas/02-ciclo-solicitacao.puml)

| Situação | Significado |
|---|---|
| `RASCUNHO` | Escolha em preparação, ainda não confirmada |
| `EM_ANALISE` | Comunicação formal feita; análises em curso |
| `PENDENTE_DOCUMENTOS` | Aguarda ação do consorciado ou do beneficiário |
| `APROVADA` | Todas as análises aprovadas |
| `REPROVADA` | Ao menos uma análise reprovada (o crédito continua disponível) |
| `EM_FORMALIZACAO` | Garantia em constituição ou registro |
| `APTA_PAGAMENTO` | Todas as pré-condições (UC13) atendidas |
| `EM_PAGAMENTO` | Pagamento(s) autorizados, aguardando confirmação, ou etapas em curso |
| `PAGA` | Pagamentos da aquisição confirmados |
| `EM_DESTINACAO_SOBRA` | Aguarda a escolha ou a execução da destinação da sobra |
| `CONCLUIDA` | Encerrada com sucesso |
| `CANCELADA` | Desistência, expiração, cessão ou exclusão |
| `SUSPENSA` | Crédito suspenso |

| De | Para | Quando |
|---|---|---|
| — | `RASCUNHO` | objeto elegível informado |
| `RASCUNHO` | `EM_ANALISE` | comunicação formal confirmada |
| `RASCUNHO` | `CANCELADA` | desistência ou expiração (`PAR-28`) |
| `EM_ANALISE` | `PENDENTE_DOCUMENTOS` | pendência |
| `PENDENTE_DOCUMENTOS` | `EM_ANALISE` | pendências sanadas |
| `EM_ANALISE` | `REPROVADA` / `APROVADA` | decisão consolidada |
| `APROVADA` | `EM_FORMALIZACAO` | aquisição: constituir garantia |
| `APROVADA` | `APTA_PAGAMENTO` | finalidade sem garantia nova (espécie, crédito parcial) |
| `APROVADA` | `EM_PAGAMENTO` | quitação de financiamento: pagar o credor primeiro |
| `EM_FORMALIZACAO` | `APTA_PAGAMENTO` | garantia registrada e UC13 ok |
| `EM_FORMALIZACAO` | `PAGA` | quitação: nova garantia registrada (janela fechada) |
| `APTA_PAGAMENTO` | `EM_PAGAMENTO` | 1º pagamento autorizado |
| `EM_PAGAMENTO` | `APTA_PAGAMENTO` | pagamento rejeitado |
| `EM_PAGAMENTO` | `EM_FORMALIZACAO` | quitação confirmada → constituir nova garantia |
| `EM_PAGAMENTO` | `PAGA` | todos os pagamentos/etapas confirmados |
| `PAGA` | `EM_DESTINACAO_SOBRA` / `CONCLUIDA` | há saldo / não há saldo |
| `EM_DESTINACAO_SOBRA` | `CONCLUIDA` | sobra destinada |
| `EM_ANALISE` … `APTA_PAGAMENTO` | `CANCELADA` | desistência, cessão ou exclusão, sem pagamento enviado |
| qualquer não final | `SUSPENSA` ↔ anterior | crédito suspenso / reativado |

## 3. Garantia

Diagrama: [`diagramas/03-ciclo-garantia.puml`](diagramas/03-ciclo-garantia.puml)

| Situação | Significado |
|---|---|
| `PROPOSTA` | Garantia definida, instrumento em preparação |
| `EM_ASSINATURA` | Instrumento enviado para assinatura |
| `ASSINADA` | Todas as partes assinaram |
| `EM_REGISTRO` | Protocolada no órgão de registro |
| `EM_EXIGENCIA` | Registro com exigência a cumprir |
| `REGISTRADA` | Válida contra terceiros; cobertura confirmada |
| `SUBSTITUIDA` | Trocada por outra já registrada |
| `EM_EXECUCAO` | Inadimplência pós-utilização; com o Jurídico |
| `EXECUTADA` | Bem alienado ou garantia honrada |
| `BAIXA_SOLICITADA` | Cota quitada; baixa pedida ao registro |
| `BAIXADA` | Liberada |
| `CANCELADA` | Não chegou a valer (solicitação cancelada) |

Garantias sem registro externo (fiança, aval) passam de `ASSINADA` direto a `REGISTRADA`. `REGISTRADA → BAIXA_SOLICITADA` exige a cota quitada (RN-12). `REGISTRADA → SUBSTITUIDA` só depois de a nova estar `REGISTRADA`.

## 4. Pagamento

Diagrama: [`diagramas/04-ciclo-pagamento.puml`](diagramas/04-ciclo-pagamento.puml)

| Situação | Significado | Efeito no saldo do crédito |
|---|---|---|
| `PENDENTE_APROVACAO` | Preparado, aguardando o aprovador | Valor **comprometido** |
| `APROVADA` | Aprovado (quatro olhos) | — |
| `ENVIADA` | Entregue ao banco | — |
| `EM_VERIFICACAO` | Sem confirmação no prazo; consultando o banco | — |
| `LIQUIDADA` | Pagamento confirmado | Comprometimento vira **pagamento** no extrato |
| `REJEITADA` | Recusado pelo banco | Comprometimento desfeito |
| `CANCELADA` | Recusado pelo aprovador ou cancelado antes do envio | Comprometimento desfeito |
| `DEVOLVIDA` | Recebedor devolveu o valor | **Estorno** volta ao saldo |

## 5. Etapa de cronograma
`PREVISTA → MEDICAO_SOLICITADA → MEDIDA → (NAO_CONFORME → MEDICAO_SOLICITADA) | AUTORIZADA → LIBERADA → PAGA`

## 6. Cessão
`SOLICITADA → EM_ANALISE → APROVADA → CONCLUIDA` | `EM_ANALISE → REPROVADA` | `SOLICITADA/EM_ANALISE → CANCELADA`

## 7. Sinistro
`AVISADO → EM_REGULACAO → INDENIZADO | NEGADO → ENCERRADO`
