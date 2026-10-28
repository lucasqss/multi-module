# Kit para aplicar o modelo em camadas em outro projeto

Tudo o que um agente de IA (Copilot CLI ou outro) precisa para migrar um projeto **Quarkus + Oracle existente** para
o modelo deste repositório:

- um módulo Maven;
- cinco camadas por pacote;
- a entidade JPA como modelo, com o JPA confinado;
- fronteiras garantidas por testes do ArchUnit.

A pasta é **autocontida**: o agente, no outro projeto, não precisa enxergar este repositório.

## O que tem aqui

| Arquivo | Para quem | O que é |
|---|---|---|
| [`GUIA.md`](GUIA.md) | o agente | **A fonte principal:** premissas, as 20 decisões técnicas com o porquê, a estrutura alvo, a configuração, o roteiro de migração em fases, o que fazer com código legado que não se encaixa, armadilhas e o que nunca fazer. |
| [`MODELOS.md`](MODELOS.md) | o agente | Os arquivos reais do projeto de referência, que compilam e passam nos testes: entidades, State, motor, repositories, integração, REST, fila, POM, `application.properties` e testes. Gerado por `gerar-modelos.py`. |
| [`ArquiteturaTest.java`](ArquiteturaTest.java) | o projeto | As regras do ArchUnit, com modo de migração: congela as violações existentes e barra só as novas. |
| [`AGENTS.md`](AGENTS.md) | o projeto (raiz) | As instruções permanentes do projeto migrado, com marcadores que o agente preenche. |
| [`copilot-instructions.md`](copilot-instructions.md) | o projeto (`.github/`) | O curto que o Copilot lê sempre: siga o `AGENTS.md`, uma fase por vez, pare para revisão. |
| [`PROMPT.md`](PROMPT.md) | você | O texto para colar na CLI do Copilot em cada fase, e o que fazer se ele desviar. |

## Como usar

1. **Copie esta pasta** para o outro projeto, em `docs/kit-camadas/`, e faça commit.
2. **Abra a CLI** na raiz dele (`copilot`) e cole o prompt da **Fase 0** do [`PROMPT.md`](PROMPT.md). O agente só
   escreve o diagnóstico (`docs/migracao/diagnostico.md`) e para com perguntas para você.
3. **Responda, revise e siga fase a fase.** Cada fase termina com `mvn verify` verde, um commit e um resumo.

| Fase | O que acontece | O que você revisa |
|---|---|---|
| 0. Diagnóstico | Mapa do projeto e de-para de cada classe para a camada. | O de-para, os padrões que se aplicam e as perguntas. |
| 1. Rede de segurança | ITs da API atual, OpenAPI de referência, `AGENTS.md` e ArchUnit em modo congelado. | Se os ITs cobrem a API toda. |
| 2. Build | Um módulo Maven só, com o build do modelo. | Que nada mudou de comportamento. |
| 3. Camadas | Uma camada por vez, movendo com histórico. | O diff de cada camada e as violações congeladas que sumiram. |
| 4. Padrões | Value objects, regras, State, compare-and-set, idempotência, Problem Details, onde se aplicam. | Mudanças de contrato: o agente pergunta antes. |
| 5. Testes e cobertura | Testes por nível, cobertura com três fontes, ArchUnit no modo normal. | A cobertura por pacote. |
| 6. Documentação | `AGENTS.md` final, ADRs e README. | O critério de pronto, item a item. |

## Para manter o kit atualizado

O `MODELOS.md` é gerado a partir do código deste repositório. Depois de mudar o código de referência, rode:

```bash
python3 docs/kit-camadas/gerar-modelos.py
```

O `GUIA.md` resume as ADRs de `docs/adr/` (principalmente a 0013 e a 0014). Decisão nova lá é decisão nova aqui.
