# 0001. Maven multi-module com deploy único

- **Status:** Substituída por [0014](0014-camadas-num-modulo-so.md)
- **Data:** 2026-10-19

## Contexto

O sistema tem regra de negócio, leitura, integrações e REST. Separar em microsserviços agora traria rede,
versionamento e deploy coordenado sem um time ou uma escala que peça isso. Num módulo só, nada impede o REST de
chamar o repository ou a regra de conhecer HTTP.

## Decisão

Um módulo Maven por responsabilidade (`jpa-core`, `processo-core`, `processo-query`, `integracoes`,
`processo-app`) e um único jar, montado pelo `processo-app`.

## Consequências

- O Maven impede o que não está no classpath: o core não tem a extensão REST, o query não depende do core.
- O que o Maven não pega, o `ArquiteturaModulosTest` pega (ex.: integrações só implementam ports).
- Módulo novo custa quatro registros: `<module>`, `dependencyManagement`, dependência no app e `MODULOS` no ArchUnit.
- Separar um módulo em serviço próprio no futuro é mudar o deploy, não o desenho.
