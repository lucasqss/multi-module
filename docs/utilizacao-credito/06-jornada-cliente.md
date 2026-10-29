# 06 — Jornada do cliente

Diagramas:
- [`04-jornada-cliente.puml`](diagramas/puml/04-jornada-cliente.puml): atividade com raias Consorciado / Administradora / Fornecedor
- [`05-sequencia-aquisicao-a-vista.puml`](diagramas/puml/05-sequencia-aquisicao-a-vista.puml)
- [`06-sequencia-quitacao-financiamento.puml`](diagramas/puml/06-sequencia-quitacao-financiamento.puml)
- [`07-sequencia-pagamento-etapas.puml`](diagramas/puml/07-sequencia-pagamento-etapas.puml)

## Etapas vistas pelo consorciado

| # | Etapa | O que o cliente faz | O que o cliente vê/recebe | Momento de atrito típico |
|---|---|---|---|---|
| 1 | Contemplação | Paga o lance, se houver | Aviso com valor da carta, prazo e checklist | Prazo curto do lance |
| 2 | Crédito disponível | Consulta e simula o uso | Saldo atualizado com rendimentos | Não entende o valor líquido (lance embutido) |
| 3 | Escolha | Escolhe o bem/serviço/financiamento e negocia | Elegibilidade imediata da escolha | Usado em grupo de subcat. II; imóvel com ônus |
| 4 | Comunicação formal | Informa objeto, fornecedor e condições | Protocolo + checklist personalizado | Dados incompletos do fornecedor |
| 5 | Documentação | Envia documentos pessoais, de renda e do objeto | Status por documento, pendências | Validade de certidões; reenvios |
| 6 | Análise | Aguarda; responde pendências | SLA contratual e motivo das pendências | Prazo sem visibilidade |
| 7 | Garantia | Assina contrato/escritura; registra | Instrumento para assinatura; status do registro | Cartório/Detran fora do controle da administradora |
| 8 | Complemento | Paga a diferença (próprios/FGTS) | Composição dos recursos | FGTS exige fluxo CAIXA paralelo |
| 9 | Pagamento | Autoriza (à vista) ou autoriza cada etapa | Comprovante ao fornecedor | Vendedor exige pagamento antes do registro |
| 10 | Entrega | Recebe o bem/serviço | — | — |
| 11 | Sobra | Escolhe a destinação | Opções e limites (10%) | Expectativa de receber a sobra sem quitar |
| 12 | Pós-uso | Paga prestações; quita | Demonstrativo mensal; termo de baixa ao quitar | Atraso > 1 prestação → execução |

## Jornada por modalidade (diferenças em relação ao tronco)

### Bem móvel
- **Etapa 3:** veículo novo (concessionária) ou usado (somente subcat. I); verificação de gravame do vendedor.
- **Etapa 7:** gravame no SNG/CRV, normalmente feito pelo vendedor/despachante.
- **Etapa 9:** o pagamento ao vendedor costuma ser condicionado à inclusão do gravame `[PRÁTICA]`.

### Imóvel pronto
- **Etapa 5:** matrícula atualizada, certidões do imóvel e do vendedor, laudo de avaliação.
- **Etapa 7:** contrato particular com AF registrado no RI (ato único — Lei 11.795 art. 45); ITBI pago.
- **Etapa 9:** pagamento ao vendedor após o registro, ou contra prenotação `[PRÁTICA]`.

### Planta / construção / reforma
- **Etapa 5:** projeto, alvará, cronograma físico-financeiro, dados da construtora.
- **Etapa 7:** garantia **provisória sem vínculo** ao bem.
- **Etapa 9:** etapas com medição; retenção final; **substituição da garantia** após habite-se/averbação.

### Serviço
- **Etapa 3:** contrato/orçamento do prestador com CPF/CNPJ.
- **Etapa 7:** garantia pessoal ou real de outro bem, sempre sem vínculo ao serviço.
- **Etapa 9:** à vista, ou por cronograma; o adiantamento exige contrato prestador × administradora.

### Quitação de financiamento
- **Etapa 3:** obtém do credor o saldo de quitação com validade.
- **Etapa 9:** o pagamento vai ao **credor**; depois vêm a baixa do gravame anterior e o registro da nova garantia.

## Comunicações obrigatórias ao cliente

| Momento | Conteúdo | Base |
|---|---|---|
| Contemplação | Valor, prazo do lance, documentos | Res. 285 art. 2º XIV |
| Mensal | Demonstrativo Individual do Consorciado | art. 49 |
| Pendências | Motivo e prazo | art. 2º XIV-b `[CONTRATO]` |
| 180 dias sem uso | Opção de espécie (com quitação) | art. 15 §2º |
| Encerramento | Crédito disponível em espécie, com controle de recebimento | Lei 11.795 art. 31; Res. 285 art. 36 |
