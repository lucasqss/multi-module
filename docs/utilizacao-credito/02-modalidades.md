# 02 — Modalidades e suas divergências

As modalidades compartilham um **tronco comum** (disponibilizar → indicar → analisar → garantir → pagar → destinar sobra) e divergem em **cinco pontos de variação**. Na solução unificada, cada um deles é uma operação da `PoliticaModalidade` (ver [08](08-arquitetura-solucao-unificada.md)).

| Ponto de variação | Bens móveis | Bens imóveis | Serviços |
|---|---|---|---|
| **1. Elegibilidade do objeto** | Subcat. I: veículos, aeronaves, embarcações, máquinas (usado admitido); subcat. II: somente novo | Pronto, planta, terreno, construção, reforma, empreendimento | Qualquer serviço referenciado no contrato |
| **2. Garantia padrão** | AF do próprio bem (Lei 11.795 art. 14 §1º) | AF do imóvel (Lei 9.514); admite **outro imóvel** (§2º) | **Real ou pessoal sem vínculo ao bem** (§3º): fiança, aval, AF de outro bem, carta-fiança |
| Bem ainda em produção | Garantia alternativa (§3º) | Planta/obra: garantia alternativa → **substituição** na conclusão | Sempre alternativa |
| **3. Registro** | Veículo: gravame SNG/CRV dispensa outro registro (§7º). Aeronave/embarcação/máquina: registro específico `[A CONFIRMAR]` | Matrícula no RI; compra + AF por instrumento particular e **ato único** (art. 45) | Conforme a garantia (RTD, matrícula do imóvel dado em garantia) |
| **4. Avaliação** `[PRÁTICA]` | Tabela de referência (ex.: FIPE), idade máxima | Laudo de avaliação, análise de matrícula e certidões | Orçamento/contrato de prestação |
| **5. Forma de pagamento** | À vista ao vendedor; quitar o gravame do vendedor junto `[PRÁTICA]` | À vista na escritura **ou** por etapas (planta, construção, reforma) | À vista ou conforme cronograma; adiantamento só com contrato (art. 18 §2º II) |
| Documento fiscal típico `[PRÁTICA]` | NF (novo) / ATPV-e (usado) | Escritura/contrato, matrícula atualizada, certidões, ITBI | NF de serviço (PJ) ou recibo idôneo (PF) |
| FGTS | Não | Sim (lance, complemento, amortização), conforme regras CAIXA | Não |
| Execução | DL 911 (judicial ou extrajudicial com cláusula expressa) | Lei 9.514 (consolidação + leilões); saldo remanescente cobrável | Execução da garantia; contrato é título executivo extrajudicial (Lei 11.795 art. 10 §6º) |
| Despesas vinculadas típicas (sobra) | Transferência, IPVA, emplacamento, seguro | ITBI, escritura, registro, seguro | Tributos/seguros vinculados |
| Risco operacional típico | Fraude documental, gravame pré-existente | Ônus na matrícula, obra parada, falta de averbação | Prestador sem lastro, serviço não entregue |

## Finalidades transversais

### Quitação de financiamento (todas as modalidades)
1. Anuência prévia; validar **titularidade**, **mesma categoria** e **quitação total** (carta + recursos próprios, se faltar).
2. Obter do credor o saldo de quitação com validade.
3. Pagar **ao credor**, nunca ao consorciado.
4. Receber o termo de quitação e a **baixa do gravame/AF anterior**.
5. Constituir e registrar a garantia a favor do grupo.

Entre os passos 3 e 5 o grupo fica **sem garantia**. Essa **janela de risco** é controlada por timer e alerta (`bpmn/03-quitacao-financiamento.bpmn`).

### Sobra de crédito (bem mais barato que a carta), art. 18 §3º
A destinação é escolha do consorciado:
1. **despesas vinculadas**, com teto total de **10% do crédito** da contemplação;
2. abatimento de **prestações vincendas**;
3. **devolução** em conta, **somente com a cota quitada**.

### Complemento (bem mais caro que a carta) `[PRÁTICA consolidada]`
O consorciado paga a diferença direto ao vendedor, com recursos próprios ou FGTS (imóvel). A garantia recai sobre o **bem inteiro**. A composição dos recursos fica registrada para PLD.

### Crédito em espécie
Exige ≥ 180 dias da contemplação **e** quitação das obrigações. O crédito é pago em conta de titularidade do consorciado.

## Casos de borda

| Caso | Tratamento |
|---|---|
| Lance embutido | Crédito disponível = crédito da AGO − lance (art. 13). Base dos 10% da sobra: `[A CONFIRMAR]`, parametrizar |
| Crédito parado | Continua rendendo (art. 17); a norma não fixa prazo máximo de utilização |
| Terreno + construção | Duas utilizações sob o mesmo crédito: aquisição do terreno à vista + plano de etapas |
| Várias cotas para um bem | Cada cota tem fluxo, garantia e saldo próprios; consolidar na `SolicitacaoUtilizacao` como várias `ComposicaoRecursos` `[PRÁTICA]` |
| Cessão de cota contemplada | Anuência + capacidade do cessionário com evidência. Antes do uso, o cessionário herda o direito de uso; depois, herda o saldo e a garantia |
| Óbito | Contemplado: seguro amortiza o saldo, sobra ao beneficiário (art. 29) |
| “Aquisição simbólica” para liberar sobra | Alerta de PLD/fraude `[PRÁTICA]` |
| NF emitida antes da contemplação | Conflita com o ressarcimento de sinal (só o pago **após** a contemplação, art. 18 §1º) |
