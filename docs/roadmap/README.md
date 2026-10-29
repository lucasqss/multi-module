# Roadmap — Utilização da carta de crédito

Sequência de entrega das capacidades definidas no [BDUF negocial](../bduf/README.md). Cada etapa entrega **um fluxo de ponta a ponta utilizável em produção**, do crédito disponibilizado ao pagamento e à garantia registrada, e reaproveita o tronco comum construído nas etapas anteriores.

## Critérios de sequenciamento

1. **Valor e volume:** primeiro o que concentra mais créditos e mais valor.
2. **Construir o tronco comum cedo:** solicitação, documentos, análises, garantia, pagamento e sobra nascem na etapa 1 e servem a todas as outras.
3. **Do mais simples ao mais complexo no pagamento:** à vista → etapas → quitação com janela sem garantia.
4. **Risco regulatório controlado:** cada etapa só entra em operação com os seus critérios de aceite ([`../bduf/13-criterios-aceite.md`](../bduf/13-criterios-aceite.md)) aprovados.

## Etapas

| # | Etapa | Escopo | Casos de uso novos | Reaproveita | Situação |
|---|---|---|---|---|---|
| **1** | [**Aquisição de imóvel urbano pronto**](etapa-01-imovel-urbano/README.md) | Imóvel urbano construído e averbado (residencial ou comercial), pagamento à vista, carta + recursos próprios, sobra | UC14, UC01, UC02, UC03, UC23, UC04, UC05, UC06, UC13, UC07, UC11 (próprios), UC12, UC10, UC24 | — | **Em definição** |
| 2 | FGTS na aquisição de imóvel | Uso do FGTS como complemento (e no lance, se houver) | UC11 (FGTS) | Etapa 1 inteira | Planejada |
| 3 | Bens móveis — veículos | Subcategorias I e II, novo e usado, gravame | Política de bem móvel; UC06 (gravame) | Tronco da etapa 1 | Planejada |
| 4 | Quitação de financiamento | Todas as categorias; janela sem garantia | UC09, UC16 (nova AF) | Tronco + garantia | Planejada |
| 5 | Espécie e eventos de fim de ciclo | 180 dias, baixa de garantia, encerramento do grupo | UC15, UC19, UC20 | Tronco | Planejada |
| 6 | Imóvel na planta, terreno, construção e reforma | Cronograma, medições, garantia provisória e substituição, adiantamento | UC08, UC16 | Etapa 1 + 4 | Planejada |
| 7 | Serviços | Prestadores, garantia sem vínculo, cronograma | Política de serviço | Etapa 6 (etapas) | Planejada |
| 8 | Eventos da cota | Cessão, óbito/sinistro, exclusão com crédito parcial, execução de garantia | UC17, UC21, UC22, UC18 | Tronco | Planejada |
| 9 | Demais bens móveis | Máquinas, equipamentos, embarcações, aeronaves | Registros específicos | Etapa 3 | Planejada |

> **Atenção à ordem 5 × 1:** a baixa de garantia (UC19) só é necessária quando a primeira cota com imóvel adquirido for quitada, o que pode levar anos. Mesmo assim, ela precisa estar pronta **antes** desse momento. A execução de garantia (UC18) pode ser necessária logo após a etapa 1, porque o atraso de mais de uma prestação já dispara a execução. Recomendo **antecipar o UC18 para logo após a etapa 1** ou ter um procedimento manual de contingência no dia da entrada em operação.

## Como cada etapa é documentada

```
etapa-NN-nome/
  README.md      escopo, fases, responsáveis, regras, documentos, decisões, critérios de pronto
  processo-*.bpmn processo específico da etapa (abre no bpmn.io / Camunda Modeler)
```
