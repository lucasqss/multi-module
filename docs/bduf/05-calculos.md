# 05 — Cálculos

## 1. Convenções de negócio

| Item | Regra |
|---|---|
| Moeda | Real, com 2 casas decimais em todo valor comunicado ou pago |
| Arredondamento | Ao centavo, só no valor final de cada movimento; critério definido pela Contabilidade (`PAR-31`). Resíduos de rateio ficam na última parcela/etapa |
| Datas | Datas de negócio no horário de Brasília |
| Dias úteis | Excluem sábados, domingos e feriados nacionais, estaduais e municipais **da localidade de constituição do grupo** (Res. 285 art. 52) |
| Dias corridos | Todos os dias do calendário |

## 2. Extrato do crédito e saldos

Todo valor que entra ou sai do crédito é um **movimento** do extrato. Movimentos não são apagados nem alterados; correções entram como novos movimentos.

| Movimento | Efeito | Quando |
|---|---|---|
| Crédito inicial | + | Disponibilização: crédito da AGO − lance embutido |
| Rendimento | + | Rendimento **líquido** da aplicação (Res. 285 art. 17) |
| Atualização | ± | Índice do contrato (`PAR-03`) |
| Comprometimento | − | Pagamento preparado (ainda não confirmado) |
| Liberação de comprometimento | + | Pagamento confirmado, cancelado ou rejeitado |
| Pagamento | − | Pagamento confirmado pelo banco |
| Estorno de pagamento | + | Valor devolvido pelo recebedor |
| Amortização de vincendas | − | Sobra usada em prestações; quitação para espécie |
| Retorno ao fundo comum | − | Diferença do crédito parcial; cancelamento da contemplação |
| Ajuste | ± | Retificação, com dupla aprovação |

```
saldo utilizável = soma de todos os movimentos              (o comprometido já está descontado)
saldo aplicado   = saldo utilizável + valores comprometidos (é o que rende)
```
**Regra:** o saldo utilizável nunca fica negativo. Um pagamento que o deixaria negativo é recusado (MOT-050).

## 3. Rendimentos (RN-04)

Os recursos do grupo são aplicados em conjunto. Cada crédito recebe uma parte proporcional do rendimento líquido do grupo:

```
rendimento do crédito no dia = rendimento líquido do grupo no dia
                             × saldo aplicado do crédito no dia anterior
                             ÷ total aplicado do grupo no dia anterior
```
- Apropriação diária ou mensal (`PAR-02`).
- O rendimento corre **até o dia do pagamento** ("até a utilização efetiva").
- "Líquido" = depois de tributos e custos da aplicação; o tratamento fiscal é definido pela Contabilidade `[A CONFIRMAR]`.

## 4. Atualização após a contemplação (RN-20) `[CONTRATO]`

```
atualização do período = saldo aplicado × (índice no fim ÷ índice no início − 1)
```
O índice e a periodicidade vêm do regulamento (`PAR-03`); `NENHUM` se o contrato não prevê. Aplicar atualização e rendimento sobre a mesma base exige confirmação jurídica `[A CONFIRMAR]`.

## 5. Sobra e teto de 10% (RN-16)

```
base do teto         = crédito da AGO                        (se PAR-01 = CREDITO_AGO)
                     = crédito da AGO − lance embutido       (se PAR-01 = CREDITO_LIQUIDO)
teto para despesas   = 10% × base do teto
disponível p/ desp.  = menor valor entre (teto − despesas já destinadas) e o saldo utilizável
```
- Itens de despesa admitidos: lista por regime (`PAR-09`).
- Devolução da sobra: valor ≤ saldo utilizável **e** cota quitada.

## 6. Limite para o objeto, complemento e sinal

```
limite do objeto     = menor valor entre negociado e avaliado          [PRÁTICA] (PAR-10)
valor da carta usado = menor valor entre limite do objeto e saldo utilizável
complemento          = valor negociado − valor da carta usado          (próprios / FGTS)
sobra prevista       = saldo utilizável − valor da carta usado
```
- A garantia cobre o **bem inteiro** (valor avaliado), qualquer que seja a composição.
- Sinal ressarcível (RN-22): o vendedor recebe `valor da carta usado − sinal` e o consorciado recebe o `sinal`. No `RES_285`, as duas parcelas saem do crédito.

## 7. Crédito parcial do excluído contemplado

(Res. 285 arts. 32-A e 33)
```
crédito atualizado  = valor do crédito da categoria na data do cálculo           [CONTRATO]
bruto               = % do fundo comum pago pela cota × crédito atualizado
multa rescisória    = % do contrato × bruto           (dentro dos limites do art. 32-A; PAR-04)
crédito parcial     = bruto − multa − pendências contratuais
retorno ao fundo    = saldo aplicado − crédito parcial
```

## 8. Prazos

| Prazo | Regra |
|---|---|
| Disponibilização (RN-03) | 3º dia útil **depois** da homologação (`RES_285`) ou da contemplação (`CIRC_3432`) |
| Espécie (RN-13) | 180 dias **corridos** após a data da contemplação |
| Análise de documentos | Prazo do regulamento (`PAR-06`), contado da comunicação formal ou da última pendência sanada |
| Janela sem garantia | Prazo `PAR-25` a partir da confirmação do pagamento da quitação; alertas em 50%, 80% e 100% |
| Comunicação no encerramento | Até 60 dias após a última AGO de contemplação (Lei 11.795 art. 31) |
| Encerramento do grupo | Até 120 dias após a última AGO de contemplação (Lei 11.795 art. 32) |
| Guarda de evidências de capacidade | 5 anos após o encerramento do grupo |

**Exemplo de D+3 úteis:** homologação na quarta 18/03/2026; quinta 19/03 é feriado municipal na localidade do grupo (hipotético). Os dias úteis seguintes são sex 20/03 (1º), seg 23/03 (2º) e ter 24/03 (3º). Prazo: **24/03**. Para um grupo de outra localidade, sem o feriado, o prazo seria seg 23/03.

## 9. Etapas

```
valor da etapa      = % da etapa × valor da carta usado
retenção final      = % de retenção (PAR-12) × valor da carta usado
soma das etapas + retenção = valor da carta usado     (resíduo na última etapa)
liberação da etapa  = valor da etapa × (% medido ÷ % previsto), limitado ao valor da etapa  (PAR-24)
                      − parcela do adiantamento a abater
```

## 10. Exemplos

### 10.1 Veículo com lance embutido, sinal e sobra
| Passo | R$ |
|---|---|
| Crédito da AGO | 100.000,00 |
| Lance embutido (20%) | −20.000,00 |
| Crédito inicial | 80.000,00 |
| Rendimentos até o pagamento | +640,00 |
| **Saldo utilizável** | **80.640,00** |
| Negociado 72.000,00 · tabela de referência 75.000,00 → valor da carta usado | 72.000,00 |
| Ao vendedor (descontado o sinal de 2.000,00) | 70.000,00 |
| Ao consorciado (ressarcimento do sinal) | 2.000,00 |
| Sobra | 8.640,00 |
| Teto de despesas — `PAR-01` AGO / líquido | 10.000,00 / 8.000,00 |
| Despesas: transferência + IPVA | −3.500,00 |
| Restante → prestações vincendas | −5.140,00 |
| Saldo final | 0,00 |

### 10.2 Imóvel com complemento
| Item | R$ |
|---|---|
| Saldo utilizável | 300.000,00 |
| Negociado = avaliado | 350.000,00 |
| Valor da carta usado | 300.000,00 |
| Complemento (FGTS 30.000 + próprios 20.000) | 50.000,00 |
| Cobertura da garantia (AF do imóvel) | 350.000,00 |

### 10.3 Crédito parcial (multa hipotética de 10%)
| Item | R$ |
|---|---|
| Crédito atualizado | 100.000,00 |
| % do fundo comum pago | 25% |
| Bruto | 25.000,00 |
| Multa | −2.500,00 |
| Pendências | −300,00 |
| **Crédito parcial** | **22.200,00** |
