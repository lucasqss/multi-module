# 0007. Compare-and-set em vez de lock pessimista

- **Status:** Aceita
- **Data:** 2026-10-21

## Contexto

Duas requisições podem processar o mesmo processo ao mesmo tempo. Um lock pessimista seguraria a conexão durante a
avaliação do segmento, que pode chamar sistemas externos.

## Decisão

Transações curtas e troca de situação condicional:
`update PROCESSO set SITUACAO = :para ... where ID = :id and SITUACAO = :de`. Nenhuma linha alterada quer dizer que
outra requisição chegou antes: `ConflitoDeSituacaoException` (409). A avaliação roda fora de transação.

## Consequências

- Só uma requisição vence; o `CicloDeVidaPorTipoIT` dispara 8 simultâneas e confere.
- Se a aplicação cair entre as duas transações do processar, o processo fica em `EM_PROCESSAMENTO` e precisa de
  uma rotina de recuperação (ponto em aberto).
