# 07 — Catálogo de regras de negócio

Cada regra tem ID estável, origem, regimes de aplicação e forma de implementação. Na solução unificada, o **Resolvedor de Regras** carrega este catálogo (ver [08](08-arquitetura-solucao-unificada.md)).

Legenda de regime: **A** = `RES_285`, **C** = `CIRC_3432`, **A/C** = ambos.

## Regras normativas

| ID | Regra | Base | Tipo | Regime | Implementação |
|---|---|---|---|---|---|
| RN-01 | Nenhum crédito sem contemplação; só cota ativa e adimplente concorre | Res. 285 art. 11 | `[NORMA]` | A/C | Guarda na transição para `Homologada` |
| RN-02 | Lance só é homologado após o recebimento no prazo contratual | art. 12 II; art. 2º XIV-a | `[NORMA]` + `[CONTRATO]` | A | Timer com o prazo do regulamento |
| RN-03 | Disponibilizar até o 3º dia útil após a homologação (A) ou a contemplação (C), pelo calendário da localidade do grupo | art. 16, art. 52; Circ. art. 11 | `[NORMA]` | A/C | `CalendarioDiasUteis(grupo.localidade)` |
| RN-04 | O crédito rende da disponibilização até a utilização efetiva | art. 17; Lei art. 24 §1º | `[NORMA]` | A/C | `MovimentoCredito.RENDIMENTO` diário/mensal |
| RN-05 | Analisar capacidade de pagamento na contemplação e na cessão; reter a evidência por 5 anos após o encerramento | art. 3º §§2º-3º | `[NORMA]` | A/C | `AnaliseCapacidade.reterAte` |
| RN-06 | Pagamento direto ao vendedor/fornecedor/prestador/credor; ao consorciado só em sinal, sobra, espécie e crédito parcial | art. 18; art. 15 | `[NORMA]` | A/C | Validação na criação da `OrdemPagamento` |
| RN-07 | Pré-condições do pagamento: comunicação formal com nome + CPF/CNPJ do consorciado e do fornecedor, características do objeto, condições, garantias satisfeitas, documentos do contrato | art. 18 §2º I | `[NORMA]` | A (C: art. 13, identificação da finalidade) | *Business rule task* antes do pagamento |
| RN-08 | Adiantamento ao fornecedor só com contrato fornecedor × administradora, que assume total responsabilidade | art. 18 §2º II | `[NORMA]` | A | Bloqueio sem `ContratoAdiantamento` |
| RN-09 | O objeto deve ser da categoria/subcategoria do contrato; subcat. II só admite bem novo | art. 15 I; art. 5º §2º | `[NORMA]` | A/C | `PoliticaModalidade.validarElegibilidade` |
| RN-10 | Quitação de financiamento: total, de titularidade do consorciado, mesma categoria, com anuência prévia; inclui leasing financeiro e consórcio utilizado | art. 15 II, §1º; Lei art. 22 §3º | `[NORMA]` | A/C | `PoliticaQuitacaoFinanciamento` |
| RN-11 | A garantia inicial recai sobre o bem adquirido; outro imóvel (imóvel) ou garantia sem vínculo (serviço/bem em produção) | Lei art. 14 §§1º-3º | `[NORMA]` | A/C | `PoliticaModalidade.garantiaPadrao` |
| RN-12 | Não baixar a garantia antes da quitação integral da cota | Lei art. 14 §5º II | `[NORMA]` | A/C | Guarda em `Garantia → BAIXADA` |
| RN-13 | Espécie: ≥ 180 dias da contemplação **e** quitação das obrigações | art. 15 §2º; Circ. art. 5º XIII-d | `[NORMA]` | A/C | Timer + guarda |
| RN-14 | Vedada a exclusão de contemplado que utilizou o crédito na aquisição (A); vedada para qualquer contemplado (C) | art. 32 p.u.; Circ. art. 2º p.u. | `[NORMA]` | A/C (diferente) | Guarda no fluxo de exclusão |
| RN-15 | Contemplado que utilizou o crédito e atrasa mais de uma prestação → execução imediata da garantia | art. 27 | `[NORMA]` | A/C | Evento de cobrança |
| RN-16 | Sobra: só três destinações; despesas vinculadas ≤ 10% do crédito; devolução só com quitação | art. 18 §3º | `[NORMA]` | A/C (itens diferem) | `DestinacaoSobra` com teto |
| RN-17 | Documentação, prazos de análise e procedimento de pagamento vêm do regulamento | art. 2º XIV, XVIII | `[CONTRATO]` | A/C | `RegulamentoGrupo` |
| RN-18 | Transferência ao consorciado só com autorização e conta/Pix de sua titularidade | art. 2º XVI; art. 51 | `[NORMA]` | A/C | Validação de titularidade da conta |
| RN-19 | Monitorar pagamentos a terceiros, espécie, sobra, cessões e quitação de financiamentos | Circ. 3.978/2020 | `[NORMA]` | A/C | Checagem PLD no gateway paralelo |
| RN-20 | Atualização monetária do crédito após a contemplação conforme o contrato | art. 2º XVIII-c; Lei art. 24 | `[CONTRATO]` | A/C | `RegulamentoGrupo.indiceAtualizacaoCredito` |
| RN-22 | Sinal pago **após** a contemplação é ressarcível: deduzido do crédito (A) ou em espécie até o montante (C) | art. 18 §1º; Circ. art. 12 §1º | `[NORMA]` | A/C (diferente) | `SolicitacaoUtilizacao.sinalRessarcivel` |
| RN-23 | Imóvel em município onde a administradora opera, salvo autorização | Circ. art. 5º XIII-a-3 | `[NORMA]` | C | Validação de localização |
| RN-24 | Contemplado inadimplente que não usou o crédito: exclusão após 3 vencimentos + crédito parcial (A); cancelamento da contemplação em AGO (C) | arts. 32-33; Circ. art. 10 | `[NORMA]` | A/C (diferente) | Gateway de regime |
| RN-25 | Comunicar o crédito não utilizado até 60 dias após a última AGO de contemplação; encerrar o grupo em até 120 dias | Lei arts. 31-32 | `[NORMA]` | A/C | Processo de encerramento |
| RN-26 | FGTS segue as regras do Conselho Curador e da CAIXA | art. 53; Lei 8.036 | `[NORMA]` externa | A/C | Integração CAIXA; regras externas versionadas |

## Regras de prática (configuráveis)

| ID | Regra | Modalidade | Observação |
|---|---|---|---|
| RP-01 | Idade máxima / tabela de referência (FIPE) para usados | Móvel | Limitar o valor da carta ao avaliado |
| RP-02 | Laudo de avaliação; valor do imóvel ≥ saldo garantido | Imóvel | — |
| RP-03 | Retenção final na obra até a averbação | Imóvel | Percentual parametrizável |
| RP-04 | Quitar o gravame do vendedor junto com a compra | Móvel | — |
| RP-05 | Prazo máximo da janela sem garantia na quitação de financiamento | Todas | Ex.: 30 dias com escalonamento |
| RP-06 | Permitir quitar o saldo devedor com o próprio crédito para liberar espécie | Todas | Confirmar no regulamento |
| RP-07 | Pagamento ao vendedor somente após o registro/prenotação da garantia | Móvel/Imóvel | — |
| RP-08 | Detectar “aquisição simbólica” para liberar sobra | Todas | Alerta PLD |
| RP-09 | Limite de fornecedores/NFs por utilização | Móvel/Serviço | Não é norma |

