# 08 — Fatos de negócio e comunicações

## 1. Catálogo de fatos de negócio

Um **fato de negócio** é algo que aconteceu e que outras áreas ou partes precisam saber. Origem **E** = vem de fora do processo; **P** = produzido pelo processo.

| ID | Fato | Origem | Quem precisa saber | Desencadeia |
|---|---|---|---|---|
| FN-01 | Contemplação homologada | E (gestão do grupo) | Tesouraria | UC14 |
| FN-02 | Crédito disponibilizado | P | Consorciado, Contabilidade | Aviso de contemplação; data dos 180 dias |
| FN-03 | Prazo de disponibilização em risco/perdido | P | Tesouraria, Compliance | Tratamento imediato / ocorrência |
| FN-04 | Finalidade indicada (comunicação formal) | P | Analistas | Análises; início do SLA |
| FN-05 | Pendência aberta | P | Consorciado ou beneficiário | Ação do responsável |
| FN-06 | Pendências sanadas / checklist completo | P | Analistas | Retomada da análise |
| FN-07 | Beneficiário qualificado / reprovado | P | Consorciado, beneficiário | Libera ou bloqueia pagamento |
| FN-08 | Análise concluída | P | Analistas | Decisão consolidada |
| FN-09 | Solicitação aprovada / reprovada | P | Consorciado | Formalização ou encerramento |
| FN-10 | Garantia registrada | P | Consorciado, Tesouraria | Pré-condição de pagamento |
| FN-11 | Exigência no registro | E (registro) | Formalização, consorciado | Cumprimento da exigência |
| FN-12 | Pagamento autorizado | P | Tesouraria | Envio ao banco |
| FN-13 | Pagamento confirmado | E (banco) | Consorciado, beneficiário, Contabilidade | Sobra / conclusão |
| FN-14 | Pagamento rejeitado ou devolvido | E (banco) | Preparador, beneficiário | Correção |
| FN-15 | Etapa medida / liberada | P | Consorciado, beneficiário | Próxima etapa |
| FN-16 | Janela sem garantia aberta / encerrada / estourada | P | Formalização, Compliance | Alertas e escalonamento |
| FN-17 | Sobra destinada | P | Consorciado, gestão do grupo (vincendas), Contabilidade | — |
| FN-18 | Crédito utilizado | P | Gestão do grupo, Cobrança | Proíbe exclusão; habilita execução |
| FN-19 | Elegível à espécie (180 dias) | P | Consorciado | Opção de espécie |
| FN-20 | Crédito pago em espécie | P | Consorciado, gestão do grupo, Contabilidade | — |
| FN-21 | Cota excluída / crédito parcial pago | E / P | Excluído, Contabilidade | UC22 |
| FN-22 | Cessão concluída | P | Cedente, cessionário, gestão do grupo, garantidor | Troca de titularidade |
| FN-23 | Sinistro avisado / indenizado / negado | E / P | Atendimento, Tesouraria, beneficiários | Suspensão / retomada |
| FN-24 | Atraso pós-utilização / execução solicitada | E / P | Jurídico, consorciado, garantidor | UC18 |
| FN-25 | Cota quitada / garantia baixada | E / P | Consorciado, garantidor | UC19 |
| FN-26 | Última AGO de contemplação / crédito não procurado | E / P | Consorciados com crédito, Contabilidade | UC20 |
| FN-27 | Divergência de conciliação | P | Tesouraria, Contabilidade | Tratamento |
| FN-28 | Garantia substituída | P | Consorciado, garantidor anterior | Liberação da anterior |

## 2. Comunicações ao consorciado e às partes

| Momento | Destinatário | Conteúdo mínimo | Base |
|---|---|---|---|
| Contemplação | Consorciado | Valor do crédito, lance embutido, prazo do lance, próximos passos, documentos | Res. 285 art. 2º XIV |
| Crédito disponível | Consorciado | Saldo, como usar, data a partir da qual pode pedir espécie | arts. 15-17 |
| Pendência | Responsável | Motivo (`MOT`), o que fazer, prazo | art. 2º XIV-b |
| Aprovação / reprovação | Consorciado | Resultado, fundamentação resumida, próximos passos | — |
| Garantia para assinatura | Consorciado, garantidor | Instrumento, prazo, como registrar | art. 2º XV |
| Pagamento confirmado | Consorciado, beneficiário | Valor, data, comprovante | — |
| Sobra disponível | Consorciado | Valor, três opções, teto de despesas | art. 18 §3º |
| 180 dias | Consorciado | Opção de espécie e exigência de quitação | art. 15 §2º |
| Mensal | Consorciado | Demonstrativo individual | art. 49 |
| Atraso pós-utilização | Consorciado, garantidor | Valores em atraso, consequência (execução) | art. 27 |
| Cota quitada | Consorciado, garantidor | Termo de liberação da garantia | Lei 11.795 art. 14 §5º |
| Encerramento | Consorciados com crédito não utilizado | Crédito disponível em espécie, como pedir, prazo | Lei 11.795 art. 31 |
| Exclusão de contemplado | Excluído | Valor do crédito parcial, como receber | Res. 285 art. 33 |

## 3. Comunicações com controle de recebimento

A norma exige **prova de recebimento** em pelo menos estes casos. O canal deve gerar evidência de entrega: correspondência com AR, meio eletrônico com confirmação, ou outro meio que o Jurídico aceite.

| Caso | Base |
|---|---|
| Comunicações ao excluído | Res. 285 art. 11 §3º, art. 38 §1º |
| Comunicação de encerramento / crédito disponível | Lei 11.795 art. 31; Res. 285 art. 36 |
| Notificação de mora (pré-execução) | DL 911 / Lei 9.514 (requisito da execução) |

Quando não houver confirmação de entrega, tentar outro canal e registrar as tentativas.
