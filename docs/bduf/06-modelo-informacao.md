# 06 — Modelo de informação

Informações de que o negócio precisa, independentemente de como sejam armazenadas. A visão conceitual em UML está em [`../utilizacao-credito/diagramas/puml/02-modelo-dominio.puml`](../utilizacao-credito/diagramas/puml/02-modelo-dominio.puml).

**Legenda:** O = obrigatória · C = condicional (regra na coluna) · F = facultativa. **Dono** = área responsável pela exatidão. **Guarda** = prazo mínimo de conservação ([10 §5](10-controles.md#5-guarda-de-informações)).

## 1. Grupo e regulamento

| Informação | Obr. | Dono | Observação |
|---|---|---|---|
| Identificação do grupo | O | Gestão do grupo | — |
| Data da 1ª AGO | O | Gestão do grupo | Define o regime: `RES_285` a partir de 01/07/2024; antes, `CIRC_3432` |
| Segmento (móvel, imóvel, serviço) | O | Gestão do grupo | — |
| Subcategoria (I ou II) | C | Gestão do grupo | Obrigatória para bens móveis |
| UF e município de constituição | O | Gestão do grupo | Calendário de dias úteis |
| Área de atuação da administradora | C | Jurídico | Grupos `CIRC_3432` com imóvel (RN-23) |
| Data de encerramento | C | Gestão do grupo | Base da guarda de evidências |
| **Regulamento vigente (versão e data)** | O | Jurídico | Itens delegados pela norma ([11 §1](11-parametros-motivos.md)) |

## 2. Crédito contemplado

| Informação | Obr. | Dono | Observação |
|---|---|---|---|
| Cota e titular (CPF/CNPJ, nome) | O | Gestão do grupo | — |
| Data da AGO de contemplação | O | Gestão do grupo | Base dos 180 dias |
| Forma de contemplação (sorteio, lance livre/fixo/embutido, FGTS, indenização de seguro) | O | Gestão do grupo | — |
| Data da homologação | O | Gestão do grupo | Base do D+3 no `RES_285` |
| Valor do crédito na AGO | O | Gestão do grupo | — |
| Valor do lance embutido | C | Gestão do grupo | Se houver |
| Prazo-limite e data efetiva da disponibilização | O | Tesouraria | Prova do D+3 |
| Extrato (movimentos, datas, origem) | O | Tesouraria | Imutável |
| Saldo utilizável e valor comprometido | O | Tesouraria | Derivados do extrato |
| Situação do crédito | O | Processo | [04 §1](04-ciclos-de-vida.md) |
| Saldo devedor e % amortizado da cota | O | Gestão do grupo | Consulta; não se recalcula |

## 3. Solicitação e objeto

| Informação | Obr. | Observação |
|---|---|---|
| Finalidade | O | [`../utilizacao-credito/01-contexto-regulatorio.md §4`](../utilizacao-credito/01-contexto-regulatorio.md) |
| Data/hora e conteúdo da **comunicação formal** | O | Prova do art. 18 §2º I; início do SLA |
| Composição dos recursos (fonte, valor, comprovante) | O | Soma = valor do objeto |
| Sinal (valor, data, comprovante) | C | Se houver |
| Regulamento e versão das regras aplicadas | O | RC-1 |

**Objeto — comuns:** descrição, valor negociado, valor avaliado e método, beneficiário, indicador "em produção".

| Tipo de objeto | Informações específicas obrigatórias |
|---|---|
| Veículo / máquina / embarcação / aeronave | Subcategoria; novo ou usado; identificação (chassi/RENAVAM/placa ou nº de série/inscrição); ano de fabricação e modelo; gravame existente |
| Imóvel | Matrícula e cartório; endereço, UF e município; tipo (pronto, planta, terreno, empreendimento); operação (aquisição, construção, reforma); residencial ou não; ônus |
| Serviço | Escopo; prestador; prazo de execução; cronograma, se houver |
| Financiamento a quitar | Credor; número do contrato; tipo (crédito, arrendamento financeiro, consórcio); titular; categoria do bem; saldo de quitação e validade |

## 4. Partes

| Parte | Informações obrigatórias | Dono |
|---|---|---|
| Consorciado | CPF/CNPJ, nome, contatos, endereço, conta(s) próprias autorizadas, renda (na análise) | Atendimento |
| Beneficiário | CPF/CNPJ, nome/razão social, papel, conta de recebimento (titularidade verificada), resultado e validade da qualificação, nível de risco | PLD |
| Garantidor | CPF/CNPJ, nome, vínculo, análise de capacidade, bens oferecidos | Crédito |
| Cessionário | Idem consorciado + análise de capacidade | Crédito |
| Sucessores (sinistro) | Documento de representação (alvará, formal de partilha, inventariante) | Jurídico |

## 5. Análises e decisões

| Informação | Obr. | Observação |
|---|---|---|
| Tipo (capacidade, PLD, objeto) | O | — |
| Resultado e fundamentação | O | Texto que justifique a decisão |
| Quem decidiu e quando | O | — |
| Evidências usadas | O | Guarda: 5 anos após o encerramento (capacidade) |
| Exceções concedidas | C | Regra `[PRÁTICA]` excepcionada, justificativa e alçada |

## 6. Garantia

| Informação | Obr. | Observação |
|---|---|---|
| Tipo e bem/garantidor | O | — |
| Valor de cobertura e saldo garantido | O | Cobertura ≥ saldo |
| Provisória? Substituída por/substitui | C | Planta, obra, serviço, quitação |
| Instrumento e assinaturas | O | — |
| Órgão, protocolo, datas (prenotação, registro, baixa), exigências | C | Quando há registro externo |

## 7. Pagamento

| Informação | Obr. | Observação |
|---|---|---|
| Finalidade do pagamento | O | Aquisição, etapa, adiantamento, quitação, ressarcimento de sinal, despesa da sobra, devolução da sobra, espécie, crédito parcial, devolução de execução |
| Beneficiário e conta (ou boleto) | O | — |
| Valor e data prevista | O | — |
| Preparador, aprovador, datas | O | Pessoas distintas |
| Confirmação do banco (identificador, data) | O | Base da conciliação |
| Rejeição/devolução e motivo | C | — |

**Cronograma (etapas):** ordem, descrição, %, valor, critério de liberação, medições (%, laudo, data, responsável), autorização do consorciado, retenção final.

**Destinação da sobra:** tipo, item de despesa, valor, comprovante, pagamento ou amortização correspondente.

## 8. Comunicações

| Informação | Obr. | Observação |
|---|---|---|
| Destinatário, canal, conteúdo | O | — |
| Data de envio | O | — |
| Data de entrega/recebimento | C | Obrigatória quando a norma exige controle de recebimento ([08 §3](08-fatos-comunicacoes.md)) |
