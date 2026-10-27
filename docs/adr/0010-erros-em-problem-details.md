# 0010. Erros em Problem Details (RFC 9457)

- **Status:** Aceita
- **Data:** 2026-10-27

## Contexto

Os erros saíam num formato próprio (`{"motivo"}`), e o 404 do detalhe por id não tinha corpo. Cada cliente
precisaria aprender o formato e decidir pelo texto da mensagem.

## Decisão

Toda resposta de erro é `application/problem+json` com `type`, `title`, `status` e `detail`. O `type` é fixo por tipo
de problema (`/problemas/regra-violada`, `/problemas/transicao-invalida`, `/problemas/nao-encontrado`,
`/problemas/conflito`, `/problemas/parametro-invalido`, `/problemas/sistema-indisponivel`); o `detail` é o motivo
apresentável. O mapeamento fica num lugar só, o `TratadorDeErros`.

## Consequências

- Clientes decidem pelo `type`, não pelo texto.
- Mudou o contrato de erro: quem lia `motivo` passa a ler `detail` (o `motivo` do 200 de processamento continua).
- Erro novo do domínio: uma exceção e um método no `TratadorDeErros`.
