# 11 — Parâmetros, políticas e motivos

## 1. Parâmetros de negócio

Tudo o que **varia por grupo, regime ou administradora**. Cada parâmetro tem vigência; uma alteração vale para decisões futuras e não altera decisões passadas. Preencha os valores do seu negócio em [`../utilizacao-credito/09-parametrizacao-do-meu-sistema.md`](../utilizacao-credito/09-parametrizacao-do-meu-sistema.md).

| ID | Parâmetro | Origem | Nível | Exemplo/valores |
|---|---|---|---|---|
| PAR-01 | Base do teto de 10% da sobra | `[A CONFIRMAR]` | Regime/grupo | Crédito da AGO · crédito líquido do lance embutido |
| PAR-02 | Periodicidade de apropriação dos rendimentos | `[PRÁTICA]` | Administradora | Diária · mensal |
| PAR-03 | Índice de atualização do crédito pós-contemplação | `[CONTRATO]` art. 2º XVIII-c | Grupo | Nenhum · variação do bem · índice X |
| PAR-04 | % da multa rescisória (crédito parcial) | `[CONTRATO]` + limites do art. 32-A | Grupo | — |
| PAR-05 | Prazo de pagamento do lance | `[CONTRATO]` art. 2º XIV-a | Grupo | dias úteis |
| PAR-06 | Prazo de análise da documentação | `[CONTRATO]` art. 2º XIV-b | Grupo | dias úteis |
| PAR-07 | Finalidades habilitadas | `[CONTRATO]` | Grupo | aquisição, construção, reforma, planta, terreno, quitação, serviço… |
| PAR-08 | Garantias aceitas por modalidade | `[CONTRATO]` art. 2º XV | Grupo | AF do bem · outro imóvel · fiança · aval · carta-fiança |
| PAR-09 | Itens de despesa vinculada admitidos na sobra | `[NORMA]` por regime | Regime | ver [`../utilizacao-credito/01-contexto-regulatorio.md`](../utilizacao-credito/01-contexto-regulatorio.md) |
| PAR-10 | Limitar a carta ao menor entre negociado e avaliado | `[PRÁTICA]` | Administradora | sim/não |
| PAR-11 | Idade máxima de bem usado | `[PRÁTICA]` RP-01 | Modalidade | anos |
| PAR-12 | % de retenção final em obra | `[PRÁTICA]` RP-03 | Modalidade | % |
| PAR-13 | Pagar contra prenotação/protocolo do registro | `[PRÁTICA]` RP-07 | Modalidade | sim/não |
| PAR-20 | Validade máxima por tipo de documento | `[PRÁTICA]` | Administradora | ex.: certidões 30 dias |
| PAR-21 | Validade da qualificação de beneficiário | `[PRÁTICA]` | Administradora | meses |
| PAR-22 | Limite de comprometimento de renda | `[PRÁTICA]` | Administradora | % |
| PAR-23 | Calendário de feriados por localidade | `[NORMA]` art. 52 | Grupo | — |
| PAR-24 | Liberação de etapa medida abaixo do previsto | `[PRÁTICA]` | Modalidade | proporcional · só ao atingir 100% |
| PAR-25 | Prazo máximo da janela sem garantia | `[PRÁTICA]` RP-05 | Administradora | dias |
| PAR-26 | Dias para lembrete de sobra não destinada | `[PRÁTICA]` | Administradora | dias |
| PAR-27 | Permitir quitar o saldo com o próprio crédito para espécie | `[CONTRATO]`/`[PRÁTICA]` RP-06 | Grupo | sim/não |
| PAR-28 | Expiração de solicitação em rascunho | `[PRÁTICA]` | Administradora | dias |
| PAR-29 | Prazo para confirmação bancária antes de verificar | `[PRÁTICA]` | Administradora | horas |
| PAR-30 | Alçadas de aprovação por valor e finalidade | `[PRÁTICA]` | Administradora | faixas |
| PAR-31 | Critério de arredondamento | Contabilidade | Administradora | — |

## 2. Políticas por modalidade (o que cada uma define)

| Política | Define | Parâmetros |
|---|---|---|
| Bem móvel — subcategoria I | Elegibilidade (novo/usado, idade), avaliação, documentos, AF do bem, pagamento à vista | PAR-10, PAR-11, PAR-13 |
| Bem móvel — subcategoria II | Somente novo; documentos; AF do bem | PAR-13 |
| Imóvel pronto | Avaliação por laudo, documentos, AF (ou outro imóvel), pagamento à vista | PAR-10, PAR-13 |
| Imóvel planta / construção / reforma / terreno | Cronograma, garantia provisória e substituição, retenção, medição | PAR-12, PAR-24 |
| Serviço | Contrato do prestador, garantia sem vínculo, cronograma, adiantamento | PAR-24 |
| Quitação de financiamento | Titularidade, categoria, quitação total, janela sem garantia | PAR-25 |

## 3. Catálogo de motivos (recusa e pendência)

Texto ao cliente: curto, com o motivo e a ação esperada. **R** = recusa (a solicitação/operação não segue); **P** = pendência (segue após uma ação).

| Código | Tipo | Motivo | Mensagem sugerida | Regra |
|---|---|---|---|---|
| MOT-010 | R | Situação não permite a operação | "Esta operação não está disponível na situação atual do seu crédito." | 04 |
| MOT-011 | R | Objeto de outra categoria | "Sua carta é de {categoria}; o bem escolhido é de outra categoria." | RN-09 |
| MOT-012 | R | Bem usado em grupo de subcategoria II | "Seu grupo só permite bens novos." | RN-09 |
| MOT-013 | R | Imóvel fora da área de atuação | "O imóvel está fora da área permitida pelo seu contrato." | RN-23 |
| MOT-014 | R | Finalidade não prevista no regulamento | "Seu contrato não prevê esta forma de uso." | RN-17 |
| MOT-015 | R | Já existe solicitação em andamento | "Conclua ou cancele a solicitação em andamento." | — |
| MOT-020 | R | Financiamento não está em nome do consorciado | "O contrato a quitar precisa estar em seu nome." | RN-10 |
| MOT-021 | R | Financiamento de outra categoria | "O financiamento é de um bem de outra categoria." | RN-10 |
| MOT-022 | P | Crédito não cobre a quitação total | "Complemente a diferença para quitar o contrato inteiro." | RN-10 |
| MOT-023 | P | Saldo de quitação vencido | "Envie um novo saldo de quitação dentro da validade." | RN-10 |
| MOT-030 | P | Documento obrigatório pendente | "Falta o documento {nome}." | RN-17 |
| MOT-031 | P | Documento vencido | "O documento {nome} venceu; envie um atualizado." | PAR-20 |
| MOT-040 | P | Garantia ainda não registrada | "Aguardando o registro da garantia." | RN-07 |
| MOT-041 | R | Baixa com saldo devedor | "A garantia é liberada após a quitação da cota." | RN-12 |
| MOT-042 | P | Garantia substituta insuficiente | "A nova garantia não cobre o saldo devedor." | Lei art. 14 §5º |
| MOT-050 | R | Saldo insuficiente | "O valor supera o saldo disponível." | — |
| MOT-051 | R | Beneficiário não permitido | "O pagamento deve ser feito ao vendedor/fornecedor." | RN-06 |
| MOT-052 | P | Conta não é do consorciado | "Informe uma conta de sua titularidade." | RN-18 |
| MOT-053 | P | Adiantamento sem contrato | "Adiantamentos exigem contrato do fornecedor com a administradora." | RN-08 |
| MOT-054 | R (interno) | Aprovador igual ao preparador | — | CT-02 |
| MOT-055 | P | Beneficiário não qualificado | "Os dados do recebedor estão em análise." | RN-19 |
| MOT-056 | P | Comunicação formal incompleta | "Complete os dados do bem e do vendedor." | RN-07 |
| MOT-060 | R | Teto de despesas excedido | "O limite para despesas é {valor}." | RN-16 |
| MOT-061 | P | Devolução sem quitação | "Para receber a sobra, a cota precisa estar quitada." | RN-16 |
| MOT-062 | R | Não há sobra | "Não há saldo remanescente." | — |
| MOT-070 | R | Espécie antes de 180 dias | "O crédito em dinheiro fica disponível a partir de {data}." | RN-13 |
| MOT-071 | P | Espécie sem quitação | "Para receber em dinheiro, a cota precisa estar quitada." | RN-13 |
| MOT-080 | R | Exclusão vedada: crédito já utilizado | — (interno) | RN-14 |
| MOT-081 | R | Cessão não aprovada | "A transferência da cota não foi aprovada." | Lei art. 13 |
| MOT-090 | R | Sinal anterior à contemplação | "Só é ressarcível o sinal pago após a contemplação." | RN-22 |
