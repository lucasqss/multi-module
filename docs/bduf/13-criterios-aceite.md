# 13 — Critérios de aceite

Cenários em linguagem de negócio (Dado / Quando / Então). Servem para validar qualquer solução, seja sistema ou procedimento. Os valores seguem os exemplos de [05](05-calculos.md).

## Disponibilização (UC14)

**CA-01 — D+3 conta da homologação no RES_285**
```gherkin
Dado um grupo com 1ª AGO em 15/08/2024 (regime RES_285), constituído num município com feriado em 19/03/2026
E uma contemplação por lance em 10/03/2026, com o lance recebido e homologado em 18/03/2026
Quando o prazo de disponibilização é calculado
Então o prazo-limite é 24/03/2026
```

**CA-02 — D+3 conta da contemplação no CIRC_3432**
```gherkin
Dado um grupo com 1ª AGO em 10/02/2020 (regime CIRC_3432)
E uma contemplação por sorteio em 18/03/2026, sem feriados locais
Quando o prazo de disponibilização é calculado
Então o prazo-limite é 23/03/2026
```

**CA-03 — Lance não pago**
```gherkin
Dado uma contemplação por lance com prazo de pagamento até 20/03/2026
Quando o lance não é pago até essa data
Então o crédito não é disponibilizado
E a situação do crédito é CANCELADO
```

**CA-04 — Aviso repetido não duplica o crédito**
```gherkin
Dado que a contemplação da cota 123 já gerou um crédito
Quando o mesmo aviso de contemplação chega novamente
Então nenhum novo crédito é criado
```

## Elegibilidade (UC02, UC05)

**CA-05 — Usado em subcategoria II**
```gherkin
Dado um grupo de bens móveis da subcategoria II
Quando o consorciado escolhe um bem usado
Então a escolha é recusada com o motivo MOT-012
```

**CA-06 — Usado em subcategoria I**
```gherkin
Dado um grupo de bens móveis da subcategoria I e a idade máxima PAR-11 de 10 anos
Quando o consorciado escolhe um veículo usado de 6 anos
Então a escolha é aceita para análise
```

**CA-07 — Categoria diferente**
```gherkin
Dado um crédito de grupo de imóveis
Quando o consorciado pede para quitar o financiamento de um veículo
Então o pedido é recusado com o motivo MOT-021
```

**CA-08 — Limite pelo menor valor**
```gherkin
Dado o saldo utilizável de R$ 80.640,00 e PAR-10 ativo
E um veículo negociado por R$ 72.000,00 e avaliado em R$ 75.000,00
Quando a análise do objeto é concluída
Então o valor da carta usado é R$ 72.000,00
E a sobra prevista é R$ 8.640,00
```

## Pagamento (UC13, UC07)

**CA-09 — Pagamento ao próprio consorciado numa aquisição**
```gherkin
Dado uma solicitação de aquisição de veículo
Quando o beneficiário informado é o próprio consorciado
Então o pagamento é bloqueado com o motivo MOT-051
```

**CA-10 — Sem garantia registrada**
```gherkin
Dado uma aquisição de imóvel aprovada, com a garantia ainda em registro, e PAR-13 desativado
Quando o preparador tenta criar o pagamento
Então o pagamento é bloqueado com o motivo MOT-040
```

**CA-11 — Quatro olhos**
```gherkin
Dado um pagamento preparado pelo usuário A
Quando o usuário A tenta aprová-lo
Então a aprovação é recusada (MOT-054)
```

**CA-12 — Sem confirmação não gera um segundo pagamento**
```gherkin
Dado um pagamento enviado ao banco sem confirmação após o prazo PAR-29
Quando alguém tenta enviá-lo novamente
Então o envio só é possível depois de o banco confirmar que o primeiro falhou
```

**CA-13 — Rejeição devolve o valor ao saldo**
```gherkin
Dado um saldo utilizável de R$ 80.640,00 e um pagamento de R$ 70.000,00 comprometido
Quando o banco rejeita o pagamento
Então o saldo utilizável volta a R$ 80.640,00
```

**CA-14 — Troca de conta invalida a aprovação**
```gherkin
Dado um pagamento aprovado ao beneficiário X
Quando a conta de recebimento de X é alterada
Então o pagamento volta a exigir aprovação
```

**CA-15 — Sinal anterior à contemplação**
```gherkin
Dado uma contemplação em 10/03/2026
Quando o consorciado informa um sinal pago em 01/03/2026
Então o ressarcimento do sinal é recusado com o motivo MOT-090
```

## Sobra (UC10)

**CA-16 — Teto de 10% com base no crédito da AGO**
```gherkin
Dado um crédito da AGO de R$ 100.000,00 com lance embutido de R$ 20.000,00 e PAR-01 = CREDITO_AGO
E despesas já destinadas de R$ 3.500,00
Quando o consorciado pede R$ 7.000,00 para outras despesas vinculadas
Então o pedido é recusado com o motivo MOT-060
E o valor ainda disponível para despesas é R$ 6.500,00
```

**CA-17 — Teto com base no crédito líquido**
```gherkin
Dado o mesmo crédito e PAR-01 = CREDITO_LIQUIDO
Então o teto para despesas é R$ 8.000,00
```

**CA-18 — Devolução sem quitação**
```gherkin
Dado uma sobra de R$ 5.140,00 e uma cota com saldo devedor de R$ 40.000,00
Quando o consorciado pede a devolução da sobra
Então o pedido fica pendente com o motivo MOT-061
E as opções despesas vinculadas e prestações vincendas continuam disponíveis
```

**CA-19 — Itens de despesa por regime**
```gherkin
Dado um grupo CIRC_3432
Quando o consorciado pede o uso da sobra para pagar tarifa da administradora
Então o item não é aceito como despesa vinculada
Mas, num grupo RES_285, o mesmo item é aceito
```

## Espécie (UC15)

**CA-20 — Antes de 180 dias**
```gherkin
Dado uma contemplação em 10/03/2026
Quando o consorciado pede o crédito em espécie em 05/09/2026
Então o pedido é recusado com o motivo MOT-070, informando a data 06/09/2026
```

**CA-21 — Com 180 dias e sem quitação**
```gherkin
Dado 180 dias passados da contemplação, a cota com saldo devedor e PAR-27 desativado
Quando o consorciado pede espécie
Então o pedido fica pendente com o motivo MOT-071
```

## Quitação de financiamento (UC09)

**CA-22 — Titularidade**
```gherkin
Dado um financiamento em nome do cônjuge do consorciado
Quando o consorciado pede a quitação com a carta
Então o pedido é recusado com o motivo MOT-020
```

**CA-23 — Janela sem garantia estourada**
```gherkin
Dado uma quitação paga em 01/04/2026 e PAR-25 = 30 dias corridos
Quando em 01/05/2026 a nova garantia ainda não está registrada
Então é gerado o fato FN-16 (janela estourada)
E a Formalização e o Compliance são acionados
```

## Etapas (UC08)

**CA-24 — Adiantamento sem contrato**
```gherkin
Dado uma construção com o cronograma aprovado
Quando é pedido um adiantamento à construtora sem contrato com a administradora
Então o pagamento é bloqueado com o motivo MOT-053
```

**CA-25 — Substituição antes da baixa da provisória**
```gherkin
Dado uma obra concluída com garantia provisória (fiança)
Quando a AF do imóvel ainda não está registrada
Então a fiança não pode ser liberada
```

## Garantias e pós-utilização (UC18, UC19)

**CA-26 — Baixa só com quitação**
```gherkin
Dado uma garantia registrada e uma cota com saldo devedor de R$ 1,00
Quando é pedida a baixa da garantia
Então a baixa é recusada com o motivo MOT-041
```

**CA-27 — Execução após mais de uma prestação**
```gherkin
Dado um crédito UTILIZADO
Quando a cota passa a ter 2 prestações vencidas e não pagas
Então é gerado o fato FN-24 e o dossiê é enviado ao Jurídico
```

## Exclusão e crédito parcial (UC22)

**CA-28 — Exclusão vedada após a utilização**
```gherkin
Dado um grupo RES_285 e um crédito UTILIZADO
Quando chega o aviso de exclusão da cota
Então a exclusão é rejeitada (MOT-080) e a gestão do grupo é informada
```

**CA-29 — Crédito parcial**
```gherkin
Dado um grupo RES_285, um crédito DISPONIBILIZADO, crédito atualizado de R$ 100.000,00,
  25% do fundo comum pago, multa de 10% e pendências de R$ 300,00
Quando a cota é excluída
Então o crédito parcial é R$ 22.200,00
```

**CA-30 — Regime antigo**
```gherkin
Dado um grupo CIRC_3432 e um contemplado inadimplente sem utilização
Quando a inadimplência é tratada
Então não há exclusão com crédito parcial
E o caso vai à AGO para deliberar o cancelamento da contemplação
```

## Cessão e sinistro (UC17, UC21)

**CA-31 — Cessão de crédito utilizado**
```gherkin
Dado um crédito UTILIZADO garantido por fiança
Quando a cessão é aprovada para um novo titular
Então a conclusão exige a anuência do fiador ou uma garantia substituta registrada
```

**CA-32 — Óbito suspende pagamentos**
```gherkin
Dado um pagamento PENDENTE_APROVACAO na cota
Quando é recebido o aviso de óbito do titular
Então o crédito fica SUSPENSO e o pagamento não pode ser aprovado
```

## Regras complementares

**CA-34 — Conta de terceiro na espécie (RN-18)**
```gherkin
Dado um consorciado elegível à espécie e com a cota quitada
Quando informa uma conta em nome de outra pessoa
Então o pedido fica pendente com o motivo MOT-052
```

**CA-35 — Área de atuação só no regime antigo (RN-23)**
```gherkin
Dado um imóvel em município fora da área de atuação da administradora
Quando o grupo é CIRC_3432
Então a escolha é recusada com o motivo MOT-013
Mas, quando o grupo é RES_285, a escolha segue para análise
```

**CA-36 — Garantia de serviço sem vínculo ao objeto (RN-11)**
```gherkin
Dado uma solicitação de serviço aprovada
Quando a garantia é definida
Então só são aceitas garantias pessoais ou reais de outro bem previstas em PAR-08
```

**CA-37 — Sem contemplação não há crédito (RN-01)**
```gherkin
Dado uma cota não contemplada
Quando o consorciado tenta iniciar uma solicitação de utilização
Então a operação é recusada com o motivo MOT-010
```

**CA-38 — Rendimento até o pagamento (RN-04)**
```gherkin
Dado um pagamento que consome todo o saldo, confirmado em 15/04/2026
Então o crédito recebe rendimento até 15/04/2026
E não recebe rendimento a partir de 16/04/2026
```

**CA-39 — Guarda da evidência de capacidade (RN-05)**
```gherkin
Dado uma análise de capacidade concluída num grupo encerrado em 30/06/2030
Então a evidência é mantida pelo menos até 30/06/2035
```

**CA-40 — Documento obrigatório (RN-17)**
```gherkin
Dado o checklist de um imóvel pronto sem a matrícula atualizada
Quando a análise é concluída
Então a solicitação fica PENDENTE_DOCUMENTOS com o motivo MOT-030
```

## Encerramento (UC20)

**CA-33 — Comunicação com prova de recebimento**
```gherkin
Dado a última AGO de contemplação em 10/06/2027
Então os consorciados com crédito não utilizado são comunicados até 09/08/2027
E cada comunicação registra a confirmação de entrega
```
