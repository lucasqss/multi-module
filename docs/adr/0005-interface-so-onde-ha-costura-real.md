# 0005. Interface só onde há costura real

- **Status:** Aceita
- **Data:** 2026-10-22

## Contexto

Uma versão hexagonal completa (use case por interface, adapter que só repassa para o repository) acrescentou 21
arquivos sem aumentar a independência entre módulos. A independência vinha dos módulos Maven, das interfaces entre
eles e do ArchUnit.

## Decisão

Interface só onde há costura de verdade: entre módulos (`Segmento`), com sistema externo (`SituacaoCadastral`,
`GeradorDeDocumentos`) ou para trocar a infraestrutura nos testes (ports de persistência, `Transacoes`). Dentro de um
módulo, injeta-se a classe: o REST injeta o `MotorDeProcessamento`. Pelo mesmo motivo, mapper em classe só quando a
conversão tem lógica; conversão de uma linha é método do DTO.

## Consequências

- Menos arquivos e menos indireção: 82 → 64 arquivos de produção.
- O motor e os segmentos continuam testáveis sem Quarkus, com fakes dos ports.
- Quem quiser uma interface nova precisa apontar a costura.
