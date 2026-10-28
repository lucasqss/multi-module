# Instruções para o Copilot

- Siga o `AGENTS.md` da raiz: camadas, regras, convenções, "não faça" e definição de pronto.
- Este projeto está migrando para o modelo em camadas. Para qualquer tarefa da migração:
  - leia `docs/kit-camadas/GUIA.md` antes de começar;
  - copie a forma dos arquivos de `docs/kit-camadas/MODELOS.md`.
- Trabalhe **uma fase por vez** (GUIA.md, seção 6). Ao fim de cada fase:
  - `mvn verify` verde;
  - um commit `Migração camadas — fase N: <resumo>`;
  - um resumo do que mudou, do que ficou de fora e das perguntas em aberto.

  Depois, pare e espere a revisão.
- Nunca:
  - mude o contrato da API ou da fila;
  - edite os ITs da Fase 1 ou uma migration aplicada;
  - afrouxe ou pule teste;
  - invente regra de negócio.

  Na dúvida, pergunte.
- Rode os comandos Maven e leia a saída. Não declare verde sem ter rodado.
