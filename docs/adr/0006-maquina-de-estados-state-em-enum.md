# 0006. Máquina de estados: padrão State num enum

- **Status:** Aceita
- **Data:** 2026-10-22

## Contexto

Todos os tipos compartilham o ciclo `ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO` e `ABERTO → CANCELADO`.
`if`s sobre a situação espalhados pelo motor são onde transições inválidas escapam. A primeira versão usava uma
classe por estado (7 arquivos).

## Decisão

Padrão State (GoF) no próprio `enum Situacao`: cada constante sobrescreve só as transições que aceita, e o padrão
de todo evento é lançar `TransicaoInvalidaException`. O `Processo` (Context) só delega.

## Consequências

- A máquina inteira cabe num arquivo, e o valor persistido e o comportamento são o mesmo objeto.
- Transição nova: um método numa constante e uma linha na tabela do `SituacaoTest`.
- Estados que precisem de dados próprios (além do motivo da recusa) pediriam voltar a classes.
