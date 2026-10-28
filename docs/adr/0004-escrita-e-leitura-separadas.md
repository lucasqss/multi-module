# 0004. Escrita e leitura separadas

- **Status:** Substituída por [0014](0014-camadas-num-modulo-so.md) (a leitura continua separada, em `persistencia.consulta`)
- **Data:** 2026-10-19

## Contexto

Telas e listagens pedem campos e formatos que o domínio não precisa. Ler pelo domínio obrigaria a carregar
processos inteiros e a criar getters só para alimentar telas.

## Decisão

`processo-core` escreve; `processo-query` lê, com projeções `select new` direto das entidades do `jpa-core`. Os dois
não se enxergam e só se encontram nos resources do `processo-app`. As projeções do query já são o contrato de
resposta dos GETs.

## Consequências

- Consulta nova não toca no domínio nem nas regras.
- O HQL do query é validado na compilação, contra as mesmas entidades da escrita.
- A leitura depende do schema: mudar uma coluna afeta os dois lados (o mapeamento existe uma vez só).
