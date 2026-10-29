# 05 — Processos (BPMN 2.0)

Os arquivos ficam em [`diagramas/bpmn/`](diagramas/bpmn/). Todos trazem layout (DI) e abrem direto no Camunda Modeler ou no bpmn.io. A base legal de cada tarefa está no campo **Documentation** do elemento.

| Arquivo | Processo | Papel |
|---|---|---|
| `01-utilizacao-credito-principal.bpmn` | Utilização da carta de crédito | Tronco comum; chama os subprocessos |
| `02-pagamento-por-etapas.bpmn` | Pagamento por etapas | Planta, construção, reforma, serviço por cronograma |
| `03-quitacao-financiamento.bpmn` | Quitação de financiamento | Com controle da janela sem garantia |
| `04-eventos-pos-contemplacao.bpmn` | 4 pools independentes | Espécie (180 dias), inadimplência, baixa de garantia, encerramento do grupo |

## 1. Processo principal

**Lanes:** Consorciado · Atendimento/Cadastro · Crédito, Risco e PLD · Formalização/Garantias · Tesouraria · Fornecedor/Credor/Registros.

1. **Início:** cota contemplada em AGO.
2. **Lance?** Se sim, recebe o pagamento no prazo contratual; sem pagamento, a contemplação não é homologada (art. 12 II).
3. **Homologar** → **Disponibilizar e aplicar** (≤ D+3 úteis, calendário da localidade do grupo).
4. **Comunicar** o consorciado → ele **indica finalidade/objeto/fornecedor** e **envia documentos**.
5. **Gateway paralelo:** capacidade de pagamento ∥ KYC/PLD ∥ análise do objeto (*call activity* que resolve a `PoliticaModalidade`).
6. **Resultado:** pendência (volta ao envio de documentos) · reprovado (fim; o crédito segue aplicado) · aprovado.
7. **Finalidade:** aquisição → constituir garantia → registrar → validar pré-condições (art. 18 §2º) → à vista **ou** por etapas; quitação → subprocesso 03.
8. **Sobra?** O consorciado escolhe a destinação → execução.
9. **Registrar crédito utilizado** → fim.

## 2. Pagamento por etapas

Adiantamento opcional (exige contrato fornecedor × administradora) → laço **executar → medir → conforme? → autorizar → liberar → última etapa?** → entrega → se imóvel, **substituir a garantia provisória** → liberar a retenção final.

## 3. Quitação de financiamento

Validar (titularidade, categoria, quitação total) → saldo com validade → aporte se faltar → **pagar ao credor** → **gateway baseado em eventos**: termo de quitação recebido **ou** timer da janela de risco (escala e volta a esperar) → constituir AF → registrar → encerrar a janela.

## 4. Eventos pós-contemplação

| Pool | Gatilho | Resumo |
|---|---|---|
| Crédito em espécie | Timer 180 dias | Oferta → opção + conta própria → quitação (abatendo do crédito, se o contrato permitir) → crédito em conta |
| Inadimplência de contemplado | Prestação vencida | **Usou o crédito?** Sim → atraso > 1 prestação → executar → alienar → devolver ou cobrar o saldo. Não → **regime?** `RES_285` exclusão + crédito parcial; `CIRC_3432` cancelamento em AGO |
| Baixa de garantia | Mensagem “cota quitada” | Conferir quitação integral → termo → baixa no registro |
| Encerramento | Sinal “última AGO de contemplação” | Comunicar (≤ 60 dias) → resposta **ou** prazo de encerramento → pagar ou transferir para recursos não procurados |

## Convenções de modelagem

- **User task** = ação humana (consorciado ou analista); **Service task** = automação; **Business rule task** = avaliação de regras do catálogo (candidata a DMN); **Call activity** = ponto de variação por modalidade ou subprocesso reutilizável.
- Participantes externos (fornecedor, credor, registros) aparecem como **lanes** para leitura de ponta a ponta. Numa versão executável, transforme-os em *pools* separados com *message flows*.
- Os gateways de regime (`RES_285`/`CIRC_3432`) usam a variável de processo `grupo.regimeNormativo`.
