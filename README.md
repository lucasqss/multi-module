# Consórcio: utilização da carta de crédito

Exemplo completo da utilização da carta de crédito de consórcio, da regra negocial ao código executável.

## Conteúdo

| Pasta | O que tem |
|---|---|
| [`docs/utilizacao-credito/`](docs/utilizacao-credito/README.md) | Documentação de referência: contexto regulatório, modalidades, casos de uso, domínio, processos BPMN e regras de negócio |
| [`docs/bduf/`](docs/bduf/README.md) | BDUF negocial do pagamento da carta de crédito: escopo, ciclos de vida, cálculos, controles, critérios de aceite e rastreabilidade |
| [`docs/roadmap/`](docs/roadmap/README.md) | Roadmap de entrega, com a [etapa 1](docs/roadmap/etapa-01-imovel-urbano/README.md) (aquisição de imóvel urbano pronto) |
| [`docs/arquitetura/`](docs/arquitetura/README.md) | Desenho de arquitetura e modelo de dados |
| [`exemplo-monolito/`](exemplo-monolito/README.md) | Monólito de exemplo em Java 21 + Quarkus, com Oracle via Testcontainers |

## Rodar o exemplo

Pré-requisitos: JDK 21+, Maven e Docker.

```bash
cd exemplo-monolito
mvn test
```

Detalhes (Colima, organização dos pacotes, o que cada teste prova) no [README do monólito](exemplo-monolito/README.md).
