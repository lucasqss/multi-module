# 03 — Casos de uso

Cada caso de uso traz: ator, gatilho, pré-condições, resultado esperado, regras (`RN`/`RP` — [catálogo](../utilizacao-credito/07-regras-de-negocio.md)), fatos de negócio gerados (`FN` — [08](08-fatos-comunicacoes.md)) e motivos de recusa/pendência (`MOT` — [11](11-parametros-motivos.md)). Depois vêm o fluxo principal, as alternativas (A) e as exceções (E). As situações citadas estão em [04](04-ciclos-de-vida.md).

## Requisitos comuns a todos os casos de uso

| # | Requisito |
|---|---|
| RC-1 | Toda decisão e toda mudança de situação registra **quem**, **quando**, **por qual canal** e **qual versão** do regulamento e das regras estava vigente |
| RC-2 | Um mesmo pedido, repetido por engano, **não pode gerar dois efeitos** (duas solicitações, dois pagamentos) |
| RC-3 | Todo fato relevante é comunicado às partes previstas em [08](08-fatos-comunicacoes.md) |
| RC-4 | O consorciado só vê e age sobre as próprias cotas; o fornecedor, só sobre as solicitações em que é beneficiário |
| RC-5 | Toda recusa ou pendência informa **motivo** e **ação esperada** em linguagem simples |
| RC-6 | Regras `[NORMA]` não admitem exceção; regras `[PRÁTICA]` admitem exceção com justificativa e alçada ([02 §4](02-capacidades-papeis.md#4-alçadas-e-segregação-de-funções)) |

---

## A. Crédito e solicitação

### UC14 — Disponibilizar e aplicar o crédito
| | |
|---|---|
| Ator | Tesouraria |
| Gatilho | FN-01 Contemplação homologada (gestão do grupo) |
| Pré | Cota ativa; contemplação homologada (lance recebido no prazo `PAR-05`, se houver — RN-02) |
| Resultado | Crédito *Disponibilizado*, aplicado e rendendo; prazo D+3 cumprido |
| Regras | RN-01, RN-02, RN-03, RN-04, RN-20 |
| Fatos | FN-02, FN-03 |

1. Recebe da gestão do grupo: cota, grupo, AGO, forma de contemplação, valor do crédito e lance embutido.
2. Identifica o regime do grupo e calcula o **prazo-limite**: 3º dia útil após a homologação (`RES_285`) ou após a contemplação (`CIRC_3432`), pelo calendário da localidade do grupo (`PAR-23`; [05 §8](05-calculos.md#8-prazos)).
3. Abre o extrato do crédito com o valor disponível (crédito − lance embutido).
4. Aplica os recursos e registra a data efetiva da disponibilização.
5. Agenda a data de elegibilidade à espécie (180 dias) e avisa o consorciado.

- A1. Aviso de contemplação repetido → não gera um segundo crédito (RC-2).
- A2. Retificação pela gestão do grupo → movimento de ajuste no extrato; nada é apagado.
- E1. Faltando um dia útil para o prazo sem disponibilização → alerta à Tesouraria (FN-03). Se o prazo for perdido, registra uma ocorrência de compliance.

### UC01 — Consultar crédito, extrato e simular
| | |
|---|---|
| Ator | Consorciado; Atendimento |
| Pré | Crédito disponibilizado |
| Regras | RN-04, RN-13, RN-16 |

1. Mostra: crédito da AGO, lance embutido, rendimentos e atualização acumulados, valores comprometidos, **saldo utilizável** e solicitações em curso.
2. Mostra os prazos: data de elegibilidade à espécie e o prazo de análise da solicitação ativa.
3. **Simulação:** o cliente informa a modalidade e o valor do objeto. O processo devolve a sobra ou o complemento estimados, o teto para despesas (10%), os documentos esperados e a garantia exigida. A simulação não cria compromisso.

### UC02 — Indicar finalidade, objeto e beneficiário (comunicação formal)
| | |
|---|---|
| Ator | Consorciado (ou Atendimento em nome dele, com evidência da manifestação) |
| Pré | Crédito *Disponibilizado* ou *Em utilização*, sem outra solicitação ativa |
| Resultado | Solicitação *Em análise* ou *Pendente de documentos*; comunicação formal registrada; checklist definido |
| Regras | RN-07, RN-09, RN-10, RN-17, RN-22, RN-23 |
| Fatos | FN-04 |
| Motivos | MOT-011, MOT-012, MOT-013, MOT-014, MOT-015, MOT-090 |

1. Escolhe a finalidade, entre as habilitadas no regulamento do grupo (`PAR-07`).
2. Informa o objeto ([06](06-modelo-informacao.md) §3), o valor negociado e o beneficiário (nome, CPF/CNPJ, conta). Beneficiário ainda não qualificado → UC23.
3. O processo verifica a **elegibilidade** do objeto para a categoria e subcategoria do grupo.
4. Informa a composição dos recursos (carta, próprios, FGTS) e o sinal já pago, se houver (data e comprovante).
5. Revisa o resumo e **confirma**. A confirmação é a comunicação formal: fica registrada com data, hora e conteúdo exato.
6. O processo define o checklist de documentos (UC03) e inicia as análises.

- A1. Valor acima do saldo utilizável → a composição precisa cobrir a diferença (UC11).
- A2. Quitação de financiamento → o objeto é o contrato a quitar (UC09).
- A3. Planta, construção, reforma ou serviço por cronograma → o cronograma de pagamento é exigido antes de pagar (UC08).
- A4. Conjunto de bens → vários objetos, cada um com seu beneficiário.
- E1. Objeto de outra categoria (MOT-011); bem usado em grupo de subcategoria II (MOT-012); imóvel fora da área de atuação em grupo `CIRC_3432` (MOT-013); finalidade não prevista no regulamento (MOT-014).
- E2. Já existe solicitação ativa para o crédito (MOT-015).
- E3. Sinal pago antes da contemplação não é ressarcível (MOT-090); a solicitação segue sem o ressarcimento.

### UC03 — Enviar e gerir documentação
| | |
|---|---|
| Ator | Consorciado, Beneficiário, Analistas |
| Regras | RN-17 |
| Fatos | FN-05, FN-06 |
| Motivos | MOT-030, MOT-031 |

1. O checklist ([07](07-documentos.md)) lista os itens da finalidade e da modalidade, com obrigatoriedade, validade máxima e responsável pelo envio.
2. A parte responsável envia; o documento fica guardado de forma inalterável, com data de recebimento.
3. O analista aprova, ou rejeita com motivo.
4. Todos os obrigatórios aprovados → checklist completo (FN-06); a análise segue.

- A1. Documento substituído → a versão anterior é preservada.
- E1. Documento vencido no momento da análise (MOT-031) → volta a pendente.
- E2. Prazo contratual de análise estourado → alerta de SLA e registro (art. 2º XIV-b).

### UC23 — Qualificar beneficiário
| | |
|---|---|
| Ator | Consorciado ou Beneficiário (dados); Analista de PLD (decisão) |
| Regras | RN-07, RN-19 |
| Fatos | FN-07 |
| Motivos | MOT-055 |

1. Recebe CPF/CNPJ, nome/razão social, papel (vendedor, fornecedor, prestador, construtora, credor) e conta para recebimento.
2. Verifica a situação cadastral, a titularidade da conta, as listas restritivas e a condição de pessoa politicamente exposta.
3. Classifica o risco (baixo, médio, alto); risco alto exige decisão humana.
4. Qualificado → pode receber pagamentos pelo prazo de validade da qualificação (`PAR-21`).

- A1. Beneficiário já qualificado e dentro da validade → reaproveita.
- E1. Pagamento a beneficiário não qualificado é bloqueado (MOT-055).

---

## B. Análises

### UC04 — Analisar capacidade de pagamento e PLD
| | |
|---|---|
| Ator | Analista de crédito; Analista de PLD |
| Regras | RN-05, RN-19; Lei 11.795 art. 14 §4º |
| Fatos | FN-08 |

1. Reúne a renda comprovada, os compromissos e o saldo devedor da cota.
2. Avalia o comprometimento da renda frente à política (`PAR-22`) `[PRÁTICA]`.
3. PLD: origem dos recursos próprios, vínculo entre consorciado e beneficiário, sinais de fracionamento ou de aquisição simbólica (RP-08).
4. Decide: *Aprovado*, *Aprovado com garantia complementar*, *Pendente* ou *Reprovado*, sempre com fundamentação.
5. Guarda a evidência por 5 anos após o encerramento do grupo (RN-05).

- A1. Garantia complementar → um item a mais em UC06.
- A2. Alerta de PLD → fila de Compliance; a comunicação ao COAF segue o procedimento próprio ([10](10-controles.md)).

### UC05 — Analisar o objeto
| | |
|---|---|
| Ator | Analista de formalização; Engenharia |
| Regras | RN-09, RN-10, RN-23, RP-01, RP-02 |
| Fatos | FN-08 |

1. Avalia o valor: tabela de referência (móvel), laudo (imóvel) ou orçamento/contrato (serviço).
2. Verifica ônus: gravame no veículo; ônus ou indisponibilidade na matrícula; restrições do vendedor.
3. Define o **limite do crédito para o objeto** = menor valor entre o negociado e o avaliado `[PRÁTICA]`.
4. Decide; a diferença vira complemento obrigatório (UC11).

- A1. Ônus que será quitado no ato → condição para o pagamento.
- A2. Bem em produção (planta/obra) → garantia provisória (UC06).

**Decisão consolidada:** a solicitação é *Aprovada* quando todas as análises aprovam; basta uma reprovação para ficar *Reprovada*; qualquer pendência deixa *Pendente de documentos* (FN-09).

---

## C. Garantias

### UC06 — Constituir e registrar garantia
| | |
|---|---|
| Ator | Analista de formalização; Consorciado e garantidor (assinatura); Registro |
| Regras | RN-11, RN-07; Lei 11.795 art. 14 §§1º-3º e §7º, art. 45 |
| Fatos | FN-10, FN-11 |

1. Define a garantia conforme a modalidade: AF do próprio bem; AF de imóvel (do próprio ou de outro); fiança, aval ou AF de outro bem (serviço/bem em produção).
2. Prepara o instrumento. No imóvel, é o instrumento particular com efeito de escritura, com registro em ato único (art. 45).
3. Colhe as assinaturas.
4. Leva a registro (gravame, matrícula, RTD) e acompanha.
5. Registro concluído → *Registrada*; a cobertura precisa ser ≥ ao saldo garantido.

- A1. Exigência do cartório → *Em exigência*; tarefa para a formalização, com o prazo da prenotação controlado.
- A2. Garantia provisória → agenda a substituição (UC16) na conclusão.
- A3. Garantidor terceiro → passa também por UC04.
- E1. Cobertura insuficiente → garantia complementar.

### UC16 — Substituir garantia
| | |
|---|---|
| Gatilho | Obra concluída/averbada; pedido do consorciado; quitação de financiamento |
| Regras | art. 2º XV `[CONTRATO]`; Lei 11.795 art. 14 §5º I |
| Fatos | FN-28 |
| Motivos | MOT-042 |

1. Constitui e registra a nova garantia (UC06).
2. **Só depois** do registro da nova, libera a anterior. Nunca há intervalo sem cobertura.
- E1. Nova garantia com cobertura inferior ao saldo (MOT-042).

### UC19 — Baixar garantia
| | |
|---|---|
| Gatilho | FN-25 Cota quitada |
| Regras | RN-12 |
| Motivos | MOT-041 |

1. Confirma com a gestão do grupo o saldo devedor zero e a ausência de pendências.
2. Emite o termo de liberação e comanda a baixa no registro.
3. Baixa confirmada → *Baixada*; avisa o consorciado e o garantidor.
- E1. Saldo devedor maior que zero → baixa recusada (MOT-041).

### UC18 — Disparar execução de garantia
| | |
|---|---|
| Gatilho | FN-24 Atraso pós-utilização (mais de uma prestação, crédito já utilizado) |
| Regras | RN-15; DL 911; Lei 9.514 |

1. Marca a garantia *Em execução* e entrega o dossiê ao Jurídico (contrato, registro, notificações).
2. Recebe o resultado: valor apurado (art. 28) → informa à gestão do grupo. Saldo positivo → devolução ao consorciado; saldo negativo → cobrança.
- A1. Purgação da mora → a garantia volta a *Registrada*.

---

## D. Pagamentos

### UC13 — Verificar pré-condições de pagamento (comum a todos os pagamentos)

Usado por UC07, UC08, UC09, UC10, UC12, UC15 e UC22.

| # | Verificação | Motivo se falhar |
|---|---|---|
| 1 | Comunicação formal registrada, com identificação completa | MOT-056 |
| 2 | Beneficiário qualificado, com conta válida | MOT-055 |
| 3 | Beneficiário ≠ consorciado, salvo nas finalidades de exceção (sinal, sobra, espécie, crédito parcial, devolução de execução) | MOT-051 |
| 4 | Quando o consorciado recebe: conta de titularidade dele | MOT-052 |
| 5 | Checklist completo | MOT-030 |
| 6 | Garantia exigida registrada (ou condição contratual de pagar contra prenotação — RP-07) | MOT-040 |
| 7 | Adiantamento: contrato com o fornecedor vigente | MOT-053 |
| 8 | Saldo utilizável suficiente | MOT-050 |

### UC07 — Pagar à vista
| | |
|---|---|
| Ator | Preparador e Aprovador de pagamento (pessoas distintas); Tesouraria |
| Regras | RN-06, RN-07, RN-18, RN-22 |
| Fatos | FN-12, FN-13, FN-14, FN-18 (no 1º pagamento de aquisição) |
| Motivos | MOT-054 |

1. O preparador monta a ordem: beneficiário, valor (valor da carta para o objeto − sinal ressarcível), forma (TED, Pix, boleto) e data.
2. UC13 → ok → o valor fica **comprometido** no saldo.
3. O aprovador, dentro da sua alçada, aprova (quatro olhos).
4. A ordem segue ao banco; aguarda a confirmação.
5. Confirmação → o pagamento entra no extrato e o comprometimento é baixado (FN-13).
6. Sinal ressarcível → pagamento ao consorciado (UC12).
7. Restou saldo → UC10.

- A1. Boleto → o cedente precisa ser o beneficiário ou alguém autorizado por ele.
- A2. Pagamento agendado → o valor fica comprometido até a data.
- E1. Rejeição pelo banco → o comprometimento é desfeito; tarefa de correção dos dados (FN-14).
- E2. Aprovador igual ao preparador (MOT-054).
- E3. Sem confirmação no prazo → consultar o banco antes de qualquer reenvio. Nunca pagar de novo sem a certeza de que o primeiro falhou (RC-2).

### UC08 — Pagar por etapas / cronograma
| | |
|---|---|
| Ator | Engenharia; Consorciado; Preparador/Aprovador; Beneficiário |
| Regras | RN-06, RN-08, RP-03 |
| Fatos | FN-15 |
| Processo | `02-pagamento-por-etapas.bpmn` |

1. Aprova o cronograma: etapas (ordem, %, valor, critério de liberação) e o % de retenção final. Soma das etapas + retenção = valor da carta para o objeto.
2. Para cada etapa: o fornecedor pede a medição → a vistoria registra o % executado → o consorciado autoriza → pagamento como em UC07 (passos 2-5), no valor proporcional.
3. Na última etapa, com os documentos de conclusão (habite-se, averbação, termo de entrega): UC16, se for imóvel → libera a retenção.

- A1. Adiantamento → só com contrato fornecedor × administradora (RN-08); o valor é abatido das etapas seguintes.
- A2. Medição abaixo do previsto → liberação proporcional (`PAR-24`).
- A3. Obra parada além do prazo → alerta; troca de fornecedor por nova solicitação com o saldo.

### UC09 — Quitar financiamento próprio
| | |
|---|---|
| Regras | RN-10, RN-07, RP-05 |
| Fatos | FN-16 |
| Motivos | MOT-020, MOT-021, MOT-022, MOT-023 |
| Processo | `03-quitacao-financiamento.bpmn` |

1. Confere a titularidade (o contrato é do consorciado), a categoria e o saldo **total** de quitação, com validade até a data prevista do pagamento.
2. Complemento, se necessário (UC11).
3. UC13 → pagamento ao **credor**.
4. Abre a **janela sem garantia**, com o prazo `PAR-25`.
5. Termo de quitação + baixa do gravame/AF anterior → UC06 (nova AF a favor do grupo) → fecha a janela.

- E1. Titular diferente (MOT-020); categoria diferente (MOT-021); saldo não coberto (MOT-022); saldo de quitação vencido (MOT-023).
- E2. Janela estourada → escalonamento; garantia provisória até regularizar.

### UC10 — Destinar a sobra
| | |
|---|---|
| Ator | Consorciado; Preparador/Aprovador |
| Pré | Pagamento de aquisição confirmado e saldo > 0 |
| Regras | RN-16 |
| Fatos | FN-17 |
| Motivos | MOT-060, MOT-061, MOT-062 |

1. Mostra o saldo, o teto para despesas (10% — [05 §5](05-calculos.md#5-sobra-e-teto-de-10-rn-16)), o quanto já foi consumido e as três opções.
2. **Despesas vinculadas:** itens admitidos pelo regime (transferência, tributos, cartório, registro, seguros; no `RES_285`, também tarifas e ressarcimento à administradora). Paga ao credor da despesa ou reembolsa o consorciado com comprovante.
3. **Prestações vincendas:** pede à gestão do grupo a amortização e recebe a confirmação.
4. **Devolução:** só com a cota quitada; o crédito vai para conta do próprio consorciado.

- E1. Teto excedido (MOT-060); devolução sem quitação (MOT-061); não há sobra (MOT-062).
- A1. Sem escolha em `PAR-26` dias → lembrete; a sobra continua rendendo.

### UC11 — Complementar com recursos próprios ou FGTS
| | |
|---|---|
| Regras | RN-19, RN-26 |

1. Registra cada fonte e seu valor; a soma das fontes é igual ao valor do objeto.
2. **Recursos próprios:** pagos pelo consorciado direto ao vendedor, com comprovante antes do pagamento da carta, **ou** depositados para pagamento único `[PRÁTICA]`.
3. **FGTS** (imóvel residencial): pedido à CAIXA ([09](09-partes-externas.md)); aguarda a autorização; a liberação vai ao vendedor.
- E1. Sem comprovante do complemento → pagamento da carta bloqueado.

### UC12 — Ressarcir sinal pago após a contemplação
1. Confere: data do sinal ≥ data da contemplação; comprovante; recebedor = vendedor.
2. `RES_285`: pagamento ao consorciado, deduzido do crédito. `CIRC_3432`: em espécie até o montante.
- E1. Sinal anterior à contemplação (MOT-090).

### UC15 — Receber o crédito em espécie
| | |
|---|---|
| Gatilho | FN-19 (180 dias corridos da contemplação) |
| Regras | RN-13, RN-18, RP-06 |
| Motivos | MOT-070, MOT-071 |

1. Avisa o consorciado da elegibilidade.
2. O consorciado pede e informa uma conta própria.
3. Confere a quitação. Se não houver e o `PAR-27` permitir → amortiza o saldo devedor com o crédito.
4. UC13 → pagamento do saldo líquido ao consorciado → crédito *Pago em espécie*.
- E1. Menos de 180 dias (MOT-070); sem quitação e sem a permissão de abater (MOT-071).

### UC22 — Pagar crédito parcial ao excluído
| | |
|---|---|
| Gatilho | FN-21 Cota excluída, contemplada e **sem utilização** (`RES_285`) |
| Regras | RN-14, RN-24; Res. 285 arts. 32-A e 33 |
| Motivos | MOT-080 |
| Processo | `05-cessao-sinistro-credito-parcial.bpmn` |

1. Confere que não houve pagamento de aquisição (senão MOT-080) e que o regime é `RES_285`.
2. Calcula o crédito parcial ([05 §7](05-calculos.md#7-crédito-parcial-do-excluído-contemplado)); a diferença volta ao fundo comum.
3. Comunica o excluído com comprovação de recebimento e pede uma conta própria.
4. UC13 → pagamento ao excluído.
- A1. Excluído não localizado → recurso não procurado no encerramento (UC20).

---

## E. Eventos da cota

### UC17 — Ceder cota contemplada
| | |
|---|---|
| Ator | Cedente, Cessionário, Analistas |
| Regras | Lei 11.795 art. 13; Res. 285 art. 2º XIV-e e art. 3º §3º; RN-05, RN-19 |
| Fatos | FN-22 |
| Motivos | MOT-081 |

1. O cedente pede e indica o cessionário.
2. Capacidade e PLD do cessionário; atenção a indícios de intermediação irregular de "carta contemplada" `[PRÁTICA]`.
3. **Anuência** da administradora.
4. Crédito não utilizado → o direito de uso passa ao cessionário; solicitações em aberto são canceladas.
5. Crédito utilizado → o saldo devedor passa ao cessionário; anuência do garantidor ou substituição da garantia (UC16).
6. A gestão do grupo registra a troca de titularidade.
- E1. Cessionário reprovado ou sem anuência (MOT-081).

### UC21 — Tratar óbito ou sinistro do titular
| | |
|---|---|
| Ator | Beneficiários/espólio; Atendimento; Seguradora |
| Regras | Res. 285 arts. 14 e 29 |
| Fatos | FN-23 |

1. Recebe o aviso com a certidão → **suspende** os pagamentos pendentes da cota.
2. Abre o sinistro na seguradora.
3. Indenização paga:
   - **contemplado sem utilização:** amortiza o saldo; o crédito segue para os sucessores (alvará/inventário);
   - **contemplado com utilização:** quita o saldo → UC19 (baixa); a sobra da indenização vai aos beneficiários (art. 29);
   - **não contemplado** (fora deste processo): a gestão do grupo trata a indenização como lance (art. 14). Quando contemplar, entra em UC14 para os sucessores.
4. Sinistro negado → retoma o fluxo com os sucessores.

### UC20 — Comunicar crédito não utilizado no encerramento
| | |
|---|---|
| Gatilho | FN-26 Última AGO de contemplação |
| Regras | RN-25 |

1. Identifica os créditos não utilizados.
2. Comunica em até 60 dias, com comprovação de recebimento.
3. Pedido do consorciado → pagamento em espécie (sem exigir 180 dias).
4. Sem manifestação até o encerramento → *Não procurado*; informa à contabilidade.

---

## F. Controle

### UC24 — Conciliar
| | |
|---|---|
| Ator | Tesouraria (diário) |
| Fatos | FN-27 |

1. Compara os pagamentos confirmados pelo banco com os pagamentos registrados nos extratos dos créditos.
2. Compara a soma dos saldos dos créditos de cada grupo com o saldo aplicado informado pela Tesouraria.
3. Compara a situação da cota (gestão do grupo) com a situação do crédito.
4. Divergência → tratamento, com tipo (sem par, valor diferente, duplicidade, situação incoerente) e responsável.

---

## Necessidades de interação (sem definir telas)

| Público | Precisa conseguir |
|---|---|
| Consorciado | Ver o saldo e a linha do tempo; simular; escolher a finalidade; enviar documentos; autorizar etapas; escolher a sobra; acompanhar o registro e o pagamento |
| Beneficiário | Informar dados e conta; enviar NF, medições e documentos; saber quando recebe |
| Analistas | Trabalhar por fila, com prazo e prioridade; ver o dossiê completo da solicitação e a versão das regras aplicadas |
| Aprovador | Ver as pré-condições atendidas antes de aprovar; aprovar em lote dentro da alçada |
| Gestão | Acompanhar indicadores ([12](12-indicadores-riscos.md)) |
