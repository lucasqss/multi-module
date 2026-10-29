# Etapa 1 — Aquisição de imóvel urbano pronto

Processo: [`processo-aquisicao-imovel-urbano.bpmn`](processo-aquisicao-imovel-urbano.bpmn) · Definição completa dos UCs: [`../../bduf/03-casos-de-uso.md`](../../bduf/03-casos-de-uso.md)

## 1. Objetivo e escopo

Permitir que o consorciado contemplado de um grupo de imóveis **compre um imóvel urbano pronto**, pagando o vendedor com a carta de crédito (e, se faltar, com recursos próprios), com a **alienação fiduciária a favor do grupo registrada** na matrícula.

| Dentro | Fora (etapa futura) |
|---|---|
| Imóvel **urbano**, **construído**, com construção **averbada** na matrícula | Planta, terreno, construção, reforma (etapa 6) |
| Residencial ou comercial (se o regulamento permitir) | Imóvel rural |
| Vendedor PF ou PJ, sem ônus impeditivos | Imóvel com financiamento do vendedor a quitar no ato (ver D-E1-03) |
| Pagamento **à vista** ao vendedor | Pagamento por etapas (etapa 6) |
| Carta + **recursos próprios** | FGTS (etapa 2) |
| Sinal pago após a contemplação; sobra | Espécie (etapa 5) |
| Grupos `RES_285` e `CIRC_3432` | — |

## 2. As suas etapas e as fases propostas

Você listou 7 etapas. Proponho **16 fases** (F1 a F16), com o crédito disponibilizado (F0) como pré-requisito. As 8 fases novas (🆕) vêm de obrigações normativas ou de riscos que, sem elas, ficariam descobertos.

| Sua etapa | Fases propostas | Por que a fase nova é necessária |
|---|---|---|
| — | **F0** Crédito disponibilizado *(pré-requisito)* | Sem crédito disponibilizado no D+3 e com extrato, não há o que pagar (RN-03) |
| Criação do processo | **F1** Abertura e comunicação formal | — |
| Envio de documentos | **F2** Envio de documentos | — |
| — | **F3** Qualificação do vendedor (KYC/PLD) 🆕 | O pagamento vai a um terceiro: é preciso confirmar quem recebe e a titularidade da conta (RN-07, RN-19) |
| — | **F4** Análise de capacidade de pagamento 🆕 | **Obrigatória** na contemplação, com evidência guardada por 5 anos (Res. 285 art. 3º §§2º-3º) |
| — | **F5** Análise jurídica do imóvel e do vendedor 🆕 | A garantia só vale se a matrícula estiver limpa e a construção averbada; o vendedor não pode ter restrições que anulem a venda |
| Vistoria | **F6** Vistoria e avaliação | O laudo define o **valor avaliado**: limite da carta e cobertura da garantia |
| — | **F7** Decisão consolidada e complemento 🆕 | Se o imóvel custa mais que a carta, o complemento precisa estar comprovado antes do contrato |
| Seguro | **F8** Seguros | — |
| Redigir contrato de compra e venda | **F9** Redação do instrumento (compra e venda **com alienação fiduciária**) | Compra e garantia no mesmo instrumento: ato único no registro (Lei 11.795 art. 45) |
| Assinar contrato | **F10** Assinaturas | Inclui cônjuges (outorga) e a administradora (credora fiduciária) |
| — | **F11** ITBI 🆕 | O cartório não registra sem o ITBI pago; é responsabilidade do comprador |
| Constituir garantia junto ao cartório | **F12** Registro (prenotação → qualificação → exigências → registro) | — |
| — | **F13** Conferência do registro e pré-condições de pagamento 🆕 | Confere na matrícula que a AF ficou a favor do grupo e aplica o checklist do art. 18 §2º |
| Liberar pagamento | **F14** Liberação do pagamento | — |
| — | **F15** Entrega da posse 🆕 | Fecha a operação com o vendedor; pendências de posse viram ocorrência, não problema financeiro |
| — | **F16** Sobra e encerramento 🆕 | A sobra é comum (o imóvel custa menos que a carta); pode **reembolsar ITBI, registro e seguros** até 10% da carta (art. 18 §3º) |

## 3. Fases em detalhe

Legenda de responsável: **C** consorciado · **V** vendedor · **AT** atendimento · **CR** crédito/PLD · **FJ** formalização/jurídico/engenharia · **TS** tesouraria · **EXT** cartório/prefeitura/seguradora.

### F0 — Crédito disponibilizado (pré-requisito)
- **Resp.:** TS · **UC:** UC14
- **Conclusão:** crédito `DISPONIBILIZADO`, com extrato aberto e o consorciado avisado.

### F1 — Abertura do processo e comunicação formal
- **Resp.:** C (AT apoia) · **UC:** UC02, UC01
- **Entradas:** endereço e matrícula do imóvel; vendedor (nome, CPF/CNPJ); valor negociado; sinal pago (se houver); composição dos recursos.
- **Atividades:**
  1. Simular sobra/complemento.
  2. Validar a elegibilidade: grupo de imóveis; imóvel **urbano** (inscrição municipal de IPTU) e pronto; finalidade habilitada (PAR-07); área de atuação, se `CIRC_3432`.
  3. Confirmar a comunicação formal.
- **Conclusão:** solicitação `EM_ANALISE`; checklist gerado; prazo de análise (PAR-06) iniciado.
- **Recusas:** MOT-011, MOT-013, MOT-014, MOT-015.

### F2 — Envio de documentos
- **Resp.:** C (dele) e V (do vendedor e do imóvel) · **UC:** UC03
- **Documentos:** §5.
- **Conclusão:** checklist completo ou pendências abertas (MOT-030/031).

### F3 — Qualificação do vendedor
- **Resp.:** CR · **UC:** UC23
- **Atividades:** situação cadastral; titularidade da conta; listas restritivas; PEP; risco.
- **Conclusão:** vendedor qualificado. Se reprovado → solicitação reprovada, ou troca de conta.

### F4 — Análise de capacidade de pagamento
- **Resp.:** CR · **UC:** UC04
- **Conclusão:** aprovado / aprovado com garantia complementar / pendente / reprovado, com a **evidência guardada**.

### F5 — Análise jurídica do imóvel e do vendedor
- **Resp.:** FJ · **UC:** UC05
- **Verifica:**
  - o vendedor é o proprietário na matrícula;
  - a construção está **averbada**;
  - não há ônus impeditivos (penhora, indisponibilidade, hipoteca/AF não quitada, usufruto);
  - certidões do vendedor (risco de fraude à execução) e do imóvel (IPTU, condomínio);
  - estado civil e regime de bens do vendedor (outorga do cônjuge).
- **Conclusão:** parecer favorável, com ou sem condições.
- **Recusas/pendências:** construção não averbada → pendência ou recusa (D-E1-04); ônus de financiamento do vendedor → D-E1-03.

### F6 — Vistoria e avaliação
- **Resp.:** FJ (engenharia própria ou terceirizada) · **UC:** UC05
- **Conclusão:** laudo com o **valor avaliado** e a condição do imóvel.
- **Efeito:** valor da carta usado = menor entre negociado, avaliado e saldo utilizável (PAR-10); a garantia cobre o valor avaliado.

### F7 — Decisão consolidada e complemento
- **Resp.:** CR/FJ; C para o complemento · **UC:** UC04/UC05, UC11
- **Regra:** aprovação somente com **F3, F4, F5 e F6** aprovadas.
- **Complemento:** comprovante de pagamento ao vendedor (ou depósito, se a política permitir) **antes** de F9.

### F8 — Seguros
- **Resp.:** C adere; EXT emite · **Base:** `[CONTRATO]`/`[PRÁTICA]`
- **Típicos:**
  - **danos físicos ao imóvel** (o imóvel é a garantia do grupo);
  - **prestamista** (normalmente já vinculado à cota desde a adesão: confirmar).
- **Conclusão:** apólice/certificado emitido, com o grupo/administradora como beneficiário.
- O prêmio pode ser **reembolsado pela sobra** (art. 18 §3º lista seguros).

### F9 — Redação do instrumento
- **Resp.:** FJ · **Base:** Lei 11.795 art. 45 p.u.; Lei 9.514 art. 38
- **Conteúdo mínimo:**
  - compra e venda e **alienação fiduciária** no mesmo instrumento particular;
  - qualificação das partes;
  - descrição do imóvel conforme a matrícula;
  - preço e forma de pagamento (carta + próprios + sinal);
  - valor da dívida garantida e prazo;
  - cláusulas da Lei 9.514 (valor para leilão, prazo de carência para intimação);
  - obrigação de seguro;
  - condição de pagamento ao vendedor após o registro (D-E1-01).

### F10 — Assinaturas
- **Resp.:** FJ coordena; C, cônjuge, V, cônjuge do vendedor, administradora (e testemunhas, se exigidas)
- **Conclusão:** instrumento assinado por todos, na forma aceita pelo cartório (física ou eletrônica qualificada) `[A CONFIRMAR com o RI]`.

### F11 — ITBI
- **Resp.:** C · **EXT:** prefeitura
- **Conclusão:** guia paga; comprovante anexado.
- **Observação:** o valor pode ser **reembolsado pela sobra** (despesa vinculada, teto de 10%).

### F12 — Registro
- **Resp.:** FJ protocola; EXT qualifica e registra · **UC:** UC06
- **Atividades:**
  1. Prenotação.
  2. Qualificação do título.
  3. Se houver **nota de exigência**, cumpri-la **dentro do prazo da prenotação** `[A CONFIRMAR prazo legal vigente]`.
  4. Registro da compra e venda **e** da AF.
- **Conclusão:** matrícula com o consorciado como proprietário e a AF a favor da administradora/grupo.

### F13 — Conferência do registro e pré-condições
- **Resp.:** FJ · **UC:** UC13
- **Atividades:** conferir a matrícula registrada (proprietário, AF, valor) e aplicar as 8 verificações do UC13.
- **Conclusão:** solicitação `APTA_PAGAMENTO`.

### F14 — Liberação do pagamento
- **Resp.:** TS (preparador ≠ aprovador) · **UC:** UC07, UC12
- **Atividades:**
  1. Ordem ao vendedor = valor da carta usado − sinal ressarcível.
  2. Ordem ao consorciado do sinal (se houver).
  3. Aprovação dentro da alçada (PAR-30).
  4. Confirmação bancária.
- **Prazo:** compatível com venda à vista (art. 18).
- **Conclusão:** pagamento `LIQUIDADA`; crédito `UTILIZADO` (FN-18).

### F15 — Entrega da posse
- **Resp.:** V → C
- **Conclusão:** chaves/posse entregues; termo de entrega (recomendado).

### F16 — Sobra e encerramento
- **Resp.:** C escolhe; TS executa · **UC:** UC10, UC24
- **Opções:**
  - reembolso de ITBI, registro e seguros (até 10%);
  - prestações vincendas;
  - devolução (só com a cota quitada).
- **Conclusão:** solicitação `CONCLUIDA`; dossiê completo arquivado; conciliação do dia sem divergência.

## 4. Pontos de ordem que não podem ser trocados

| Antes | Depois | Por quê |
|---|---|---|
| F4 capacidade, F5 jurídico, F6 avaliação | F9 contrato | O contrato precisa do valor avaliado, da dívida garantida e da matrícula limpa |
| F7 complemento comprovado | F9 contrato | O preço do contrato precisa estar integralmente coberto |
| F11 ITBI | F12 registro | Exigência do cartório |
| F12 registro concluído | F14 pagamento | Pagar antes deixa o grupo sem garantia (recomendação; ver D-E1-01) |
| F14 pagamento confirmado | F16 sobra | A sobra só existe depois da aquisição efetiva (art. 18 §3º) |

## 5. Documentos desta etapa

Subconjunto de [`../../bduf/07-documentos.md`](../../bduf/07-documentos.md) §1 e §3 (coluna "Pronto").

| Documento | Resp. | Fase |
|---|---|---|
| Identificação, CPF, estado civil (e do cônjuge), comprovante de residência | C | F2 |
| Comprovante de renda | C | F2 → F4 |
| Identificação/contrato social do vendedor, estado civil e regime de bens, dados bancários | V | F2 → F3 |
| Matrícula atualizada com ônus reais (validade PAR-20) | V/FJ | F2 → F5 |
| Certidões do vendedor (cíveis, fiscais, trabalhistas) `[PRÁTICA]` | V | F2 → F5 |
| Certidão de débitos de IPTU e declaração de quitação de condomínio | V | F2 → F5 |
| Proposta/compromisso de compra e venda (preço, condições) | C/V | F1 |
| Comprovante do sinal (se houver) | C | F1 → F14 |
| Comprovante do complemento | C | F7 |
| Laudo de avaliação | FJ | F6 |
| Apólice/certificado de seguro | EXT | F8 |
| Instrumento de compra e venda com AF assinado | FJ | F10 |
| Guia e comprovante do ITBI | C | F11 |
| Matrícula registrada (pós-registro) | EXT | F12 → F13 |
| Comprovante de pagamento ao vendedor | TS | F14 |
| Termo de entrega de chaves | C/V | F15 |

## 6. Decisões a tomar para esta etapa

| # | Decisão | Opções | Recomendação |
|---|---|---|---|
| D-E1-01 | Quando pagar o vendedor | Após o registro · contra prenotação com retenção | **Após o registro** (PAR-13 desligado). É mais seguro para o grupo; o vendedor aceita porque o pagamento é garantido pelo instrumento |
| D-E1-02 | Seguros obrigatórios | Danos físicos · prestamista · ambos | **Danos físicos obrigatório**; confirmar se o prestamista já está na cota |
| D-E1-03 | Imóvel com financiamento do vendedor | Recusar na etapa 1 · aceitar com quitação no ato | **Recusar com orientação** na etapa 1; incluir junto com a etapa 4 (quitação) |
| D-E1-04 | Construção não averbada | Recusar · pendência para o vendedor averbar | **Pendência** com prazo; recusa se não regularizar |
| D-E1-05 | Imóvel comercial | Aceitar · só residencial | Seguir o regulamento do grupo (PAR-07) |
| D-E1-06 | Certidões exigidas do vendedor | Lista completa · lista reduzida (concentração na matrícula — Lei 13.097/2015) | Definir com o Jurídico `[A CONFIRMAR]` |
| D-E1-07 | Complemento | Pago direto ao vendedor · depositado para pagamento único | **Direto ao vendedor**, com comprovante (simplifica a etapa 1) |
| D-E1-08 | Assinatura | Física · eletrônica | Conforme a aceitação dos cartórios das praças atendidas |

## 7. Prazos e indicadores desta etapa

| Indicador | Meta sugerida |
|---|---|
| Comunicação formal → aprovação (F1 → F7) | ≤ PAR-06 |
| Aprovação → assinatura (F7 → F10) | ≤ 5 dias úteis |
| Protocolo → registro (F12) | Acompanhar por cartório (IN-07) |
| Registro → pagamento (F13 → F14) | ≤ 2 dias úteis |
| Ponta a ponta (F1 → F14) | ≤ 30 dias úteis (mediana) |

## 8. Critérios de aceite aplicáveis

Do BDUF: CA-01 a CA-04 (disponibilização), CA-08, CA-09, CA-10, CA-11 a CA-15, CA-16 a CA-19, CA-35, CA-37, CA-38, CA-39, CA-40.

Específicos desta etapa:

**CA-E1-01 — Construção não averbada**
```gherkin
Dado um imóvel cuja matrícula não registra a construção
Quando a análise jurídica é concluída
Então a solicitação fica PENDENTE_DOCUMENTOS pedindo a averbação ao vendedor
```

**CA-E1-02 — Imóvel com financiamento do vendedor (D-E1-03 = recusar)**
```gherkin
Dado uma matrícula com alienação fiduciária a favor de um banco, em nome do vendedor
Quando a análise jurídica é concluída
Então a solicitação é recusada com orientação para quitação prévia pelo vendedor
```

**CA-E1-03 — Pagamento só após o registro (D-E1-01)**
```gherkin
Dado o instrumento assinado, o ITBI pago e o título apenas prenotado
Quando o preparador tenta criar o pagamento ao vendedor
Então o pagamento é bloqueado com o motivo MOT-040
```

**CA-E1-04 — AF a favor do grupo conferida**
```gherkin
Dado o registro concluído
Quando a matrícula registrada não mostra a alienação fiduciária a favor da administradora
Então a solicitação não fica APTA_PAGAMENTO e é aberta uma exigência de retificação
```

**CA-E1-05 — Reembolso de ITBI pela sobra**
```gherkin
Dado uma carta de R$ 300.000,00 sem lance embutido, um imóvel de R$ 280.000,00 pago e um ITBI de R$ 5.600,00 pago pelo consorciado
Quando o consorciado escolhe reembolsar o ITBI com a sobra
Então R$ 5.600,00 é pago ao consorciado como despesa vinculada
E o teto restante para despesas é R$ 24.400,00
```

**CA-E1-06 — Complemento antes do contrato**
```gherkin
Dado um imóvel de R$ 350.000,00 e um saldo utilizável de R$ 300.000,00
Quando o comprovante do complemento de R$ 50.000,00 não foi anexado
Então a redação do instrumento (F9) não é iniciada
```

## 9. Definição de pronto da etapa 1

- [ ] As 16 fases operam de ponta a ponta para `RES_285` e `CIRC_3432`
- [ ] D-E1-01 a D-E1-08 decididas e refletidas nos parâmetros
- [ ] PAR-01, 06, 07, 08, 10, 13, 20, 21, 22, 25, 29, 30 e 31 com valores definidos
- [ ] Checklist de documentos validado pelo Jurídico
- [ ] Modelo de instrumento de compra e venda com AF aprovado pelo Jurídico e testado em pelo menos um cartório de cada praça
- [ ] Critérios de aceite da §8 aprovados
- [ ] Comunicações ao cliente (BDUF 08 §2) com os textos aprovados
- [ ] Conciliação diária (UC24) funcionando
- [ ] **Contingência para execução de garantia (UC18)** definida, ainda que manual (ver o alerta no [roadmap](../README.md))
- [ ] Indicadores da §7 sendo medidos

## 10. Riscos específicos

| Risco | Mitigação |
|---|---|
| Prenotação vence durante a exigência | Controle do prazo em F12; prioridade da formalização |
| Fraude documental (vendedor não é o proprietário) | F3 + F5 cruzando a identidade com a matrícula |
| Vendedor exige pagamento antes do registro | Explicar a condição desde a F1; instrumento com cláusula de pagamento pós-registro |
| Avaliação abaixo do negociado | Complemento (F7) ou renegociação; comunicar cedo |
| Atraso de cartório | Indicador por cartório; expectativa de prazo informada ao cliente |
| Matrícula com ônus que surge entre a análise e o registro | Matrícula atualizada próxima à assinatura (validade PAR-20) |
