# 03 — Casos de uso

Diagrama: [`diagramas/puml/01-casos-de-uso.puml`](diagramas/puml/01-casos-de-uso.puml)

## Atores

| Ator | Papel |
|---|---|
| Consorciado (contemplado) | Indica a finalidade, envia documentos, autoriza pagamentos e escolhe a destinação da sobra |
| Vendedor / Fornecedor / Prestador / Construtora | Recebe o pagamento e entrega o bem/serviço; solicita medições |
| Credor do financiamento | Informa o saldo, recebe a quitação e baixa o gravame anterior |
| Registros (Detran-SNG, RI, RTD) | Registra e baixa garantias |
| CAIXA (FGTS) | Autoriza e libera o FGTS (imóvel) |
| Analista (crédito, PLD, formalização) | Analisa capacidade, objeto e documentos; constitui garantias |
| Tesouraria | Disponibiliza o crédito e executa os pagamentos |
| Cobrança / Jurídico | Executa garantias |
| Agendador (tempo) | D+3, 180 dias, janela de risco, prazos de encerramento |

## Especificações resumidas

### UC01 — Consultar crédito disponível
- **Ator:** Consorciado. **Pré:** cota contemplada e homologada.
- **Fluxo:** exibe o crédito da AGO, o lance embutido, os rendimentos acumulados, o saldo utilizável, as pendências e os prazos (180 dias para espécie).
- **Regras:** RN-04, RN-05.

### UC02 — Indicar finalidade, objeto e fornecedor
- **Ator:** Consorciado. **Pré:** crédito disponibilizado.
- **Fluxo:** (1) escolhe a finalidade; (2) informa o objeto e o beneficiário (nome, CPF/CNPJ, conta); (3) o sistema consulta a `PoliticaModalidade` e devolve a elegibilidade e o checklist; (4) o sistema registra a **comunicação formal** com data.
- **Alternativos:** objeto fora da categoria/subcategoria → recusa motivada (RN-09); valor > crédito → UC11.
- **Regras:** RN-07, RN-09, RN-10.

### UC03 — Enviar e gerir documentação
- **Fluxo:** upload, validação de tipo/validade, controle de pendências e reenvio; os prazos de análise vêm do contrato.
- **Regras:** RN-17.

### UC04 — Analisar capacidade de pagamento e cadastro (KYC/PLD)
- **Ator:** Analista. **Fluxo:** analisa a renda × prestações vincendas e o cadastro do consorciado e do beneficiário; grava a evidência com retenção de 5 anos após o encerramento do grupo.
- **Alternativos:** capacidade insuficiente → exigir garantia complementar (Lei art. 14 §4º) ou recusar.
- **Regras:** RN-05, RN-19.

### UC05 — Analisar objeto conforme a política da modalidade
- **Fluxo:** avaliação (FIPE/laudo/orçamento), conferência documental (matrícula, certidões, CRV, NF, contrato de serviço) e verificação de ônus/gravames.
- **Regras:** RN-09, RN-17 e as políticas `[PRÁTICA]` de cada modalidade.

### UC06 — Constituir e registrar garantia
- **Fluxo:** a política define a garantia padrão; emite o instrumento; acompanha o registro externo; marca `REGISTRADA`.
- **Alternativos:** bem em produção ou serviço → garantia sem vínculo ao bem (art. 14 §3º); outro imóvel (§2º).
- **Regras:** RN-11, RN-12.

### UC07 — Pagar fornecedor à vista
- **Ator:** Tesouraria. **Pré:** UC13 aprovado.
- **Fluxo:** gera a `OrdemPagamento` ao beneficiário (TED/Pix), descontado o sinal ressarcível; registra o movimento no crédito; confirma a entrega.
- **Regras:** RN-06, RN-07.

### UC08 — Pagar por etapas / cronograma
- **Fluxo:** plano de etapas → medição → autorização do consorciado → liberação da parcela; retenção final; substituição de garantia na conclusão.
- **Alternativos:** adiantamento → exige `ContratoAdiantamento` (RN-08).
- BPMN: `bpmn/02-pagamento-por-etapas.bpmn`.

### UC09 — Quitar financiamento próprio
- BPMN: `bpmn/03-quitacao-financiamento.bpmn`. **Regras:** RN-10, RP-05.

### UC10 — Destinar sobra de crédito
- **Pré:** aquisição efetivada com valor < saldo utilizável.
- **Fluxo:** o consorciado escolhe entre despesas vinculadas (teto de 10%), prestações vincendas e devolução (exige quitação).
- **Regras:** RN-16.

### UC11 — Complementar com recursos próprios ou FGTS
- **Fluxo:** registra a `ComposicaoRecursos`; o complemento é pago pelo consorciado direto ao vendedor ou vem via CAIXA (FGTS); a garantia recai sobre o bem inteiro.

### UC12 — Ressarcir sinal pago após a contemplação
- **Regras:** só o sinal pago **após** a contemplação; `RES_285` deduz do crédito (art. 18 §1º).

### UC13 — Validar pré-condições de pagamento (transversal)
- Valida a comunicação formal completa, as características e condições do objeto, as garantias satisfeitas, os documentos obrigatórios e o contrato de adiantamento (se houver). **Regras:** RN-07, RN-08.

### UC14 — Disponibilizar e aplicar crédito
- **Ator:** Agendador/Tesouraria. Prazo de até D+3 úteis da homologação (`RES_285`) ou da contemplação (`CIRC_3432`), pelo calendário da localidade do grupo; aplicação até a utilização. **Regras:** RN-03, RN-04.

### UC15 — Receber crédito em espécie
- BPMN: `bpmn/04-eventos-pos-contemplacao.bpmn` (pool “Crédito em espécie”). **Regras:** RN-13, RN-18.

### UC16 — Substituir garantia
- Dispara na conclusão de obra/planta ou a pedido. Segue o procedimento do contrato (art. 2º XV); a administradora responde se a garantia substituta for insuficiente.

### UC17 — Ceder cota contemplada
- Anuência prévia; análise de capacidade do cessionário com evidência; após a utilização, transfere o saldo e a garantia.

### UC18 — Executar garantia
- **Pré:** crédito utilizado e atraso > 1 prestação. **Regras:** RN-15. BPMN: pool “Inadimplência de contemplado”.

### UC19 — Baixar garantia
- **Pré:** cota quitada integralmente. **Regras:** RN-12.

### UC20 — Comunicar crédito não utilizado no encerramento
- Até 60 dias após a última AGO de contemplação, com controle de recebimento; o que não for procurado vira recurso não procurado.
