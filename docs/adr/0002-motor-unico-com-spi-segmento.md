# 0002. Motor único com SPI `Segmento`

- **Status:** Aceita (o `Segmento` virou `Modalidade` na [0014](0014-camadas-num-modulo-so.md))
- **Data:** 2026-10-19

## Contexto

Há vários tipos de processo (imobiliário, veicular, outros por vir) com o mesmo fluxo de criação e o mesmo ciclo
de vida. Um fluxo por tipo duplicaria regras comuns, numeração, transações e estados.

## Decisão

Um `MotorDeProcessamento` para todos os tipos. O que varia fica atrás da interface `Segmento<D>` (código, prefixo,
regras próprias, gravação, leitura e a decisão `avaliar`). O motor descobre os segmentos pelo CDI e escolhe pela
classe dos dados. Motor e segmentos ficam no mesmo módulo, em pacotes separados.

## Consequências

- Tipo novo não toca no motor (o veicular entrou assim).
- O ArchUnit garante que o motor não conhece segmentos e que segmentos não se enxergam.
- Se um tipo precisar de estados ou etapas que os outros não têm, ele ganha fluxo próprio: encher o `Segmento` de
  flags é o sinal de que a decisão deixou de servir.
