# 0008. Sistemas externos atrás de ports em `integracoes`

- **Status:** Substituída por [0014](0014-camadas-num-modulo-so.md)
- **Data:** 2026-10-23

## Contexto

O negócio consulta o cadastro do CPF e pede o PDF de abertura a um sistema externo. Trazer REST Client e modelos
externos para o core contaminaria a regra; colocar no app o transformaria num depósito de entrada e saída.

## Decisão

O core declara o que precisa como port (`SituacaoCadastral`, `GeradorDeDocumentos`). O módulo `integracoes` tem
um pacote por sistema (`cadastro`, `pdf`) com `client/`, `dto/` (o formato do sistema externo, sem tipos do domínio),
`adapter/` (implementa o port e traduz erros para `SistemaExternoIndisponivelException`) e uma simulação para o
perfil dev. O PDF é pedido pelo motor depois do commit, e falha nele só vai para o log.

## Consequências

- O core não conhece HTTP nem os modelos externos; testa com fakes.
- Renomear algo no domínio não muda o contrato com o sistema externo sem passar pelo `dto/`.
- Sem o módulo no deploy, o build do Quarkus falha (port sem implementação), não a produção.
- O PDF é síncrono e sem retentativa; se não puder se perder, o próximo passo é uma outbox.
