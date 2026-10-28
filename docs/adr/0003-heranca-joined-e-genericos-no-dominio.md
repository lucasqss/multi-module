# 0003. Herança JOINED no banco, genéricos no domínio

- **Status:** Substituída por [0014](0014-camadas-num-modulo-so.md) (a herança JOINED continua; os genéricos saíram)
- **Data:** 2026-10-19

## Contexto

O processo de cada tipo é "filho" do processo: dados comuns em todos, dados próprios em cada tipo. No domínio,
records não herdam, e `sealed` não funciona entre módulos Maven.

## Decisão

- **Banco:** herança JPA `JOINED`, com `PROCESSO` (pai, discriminador `SEGMENTO`) e uma tabela filha por tipo
  (PK = FK).
- **Domínio:** composição genérica, `Processo<D extends DadosDoSegmento>`. Um processo imobiliário é um
  `Processo<DadosImobiliarios>`.
- Cada segmento converte o próprio `Processo<D>` na própria entidade filha.

## Consequências

- A listagem consulta só o pai e já inclui tipos novos; o detalhe consulta a filha.
- Tipo novo é sempre uma migration nova com a tabela filha.
- Update numa hierarquia JOINED via HQL usa tabelas temporárias: a troca de situação usa SQL nativo (ver ADR 0007).
