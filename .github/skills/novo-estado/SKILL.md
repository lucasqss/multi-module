---
name: novo-estado
description: Adiciona ou altera um estado, uma transição ou uma ação dependente de status na máquina de estados do boleto, usando o State pattern (domain.state). Use quando pedirem um novo status, uma nova transição, ou "só pode X quando estiver em Y".
---

# Novo estado ou transição (State pattern)

Comportamento que depende do status **nunca** vira `if (status == ...)` ou `switch` em entidade ou
use case. Ele mora nas classes de `src/main/java/com/example/boleto/domain/state/`.

## Conceitos do modelo atual

- `EstadoBoleto` (base `abstract sealed`): toda ação lança `TransicaoEstadoInvalidaException` (409)
  por padrão. Cada estado concreto é `final`, singleton `INSTANCIA`, e sobrescreve **só** o permitido.
- Transições **retornam** o próximo estado; a entidade faz `estado = estado.acao()`.
- Ações que não mudam o estado mas dependem dele: `validarAlteracao()`, `validarExclusao()`.
- Consultas: `emAberto()`. O adapter deriva queries delas (não duplique a lista de status).
- Auto-transição é válida: `EstadoNotificado.notificar()` retorna `this` (reenvio).

## A) Nova transição entre estados existentes

1. Na base `EstadoBoleto`: novo método que lança `acaoNaoPermitida("<verbo>")`.
2. Nos estados que permitem: sobrescreva retornando o destino.
3. Na entidade `Boleto`: método de negócio no padrão
   ```java
   public void estornar(Motivo motivo) {
       var proximo = estado.estornar();   // 1. estado autoriza (409)
       // 2. specs, se houver regra sobre os dados (422)
       estado = proximo;                  // 3. muda
       motivoEstorno = motivo;
   }
   ```
4. Use case + endpoint: skill `novo-caso-de-uso` (`POST /{id}/estorno`).

## B) Novo estado

1. Adicione o valor em `domain/enums/StatusBoleto.java` e atualize o diagrama no Javadoc.
2. Crie `domain/state/Estado<Nome>.java` (modelo: `EstadoPago` para final, `EstadoNotificado` para
   intermediário): `public final class`, `static final ... INSTANCIA`, construtor `private`,
   `status()`, e sobrescreva só as ações permitidas e consultas (`emAberto()`).
3. Inclua a classe no `permits` de `EstadoBoleto` e no `switch` de `EstadoBoleto.de(...)`.
   O compilador acusa se faltar.
4. Sobrescreva, nos estados de origem, as transições que levam ao novo estado.
5. Persistência: o status é `@Enumerated(STRING)`; confira o `length` da coluna em `BoletoJpaEntity`.

## Testes (obrigatório)

- `EstadoBoletoTest`: atualize a tabela `PERMITIDAS` (e `ACOES`, se houver ação nova). Ela testa
  **todas** as combinações (estado × ação), inclusive as proibidas.
- Atualize `somenteEmitidoPermiteAlterarEExcluir` / `emAberto` se a semântica mudou.
- `BoletoTest`: o ciclo que passa pelo novo estado e a ordem 409 antes de 422.
- `BoletoResourceTest`: transição via HTTP e `409` com `TRANSICAO_ESTADO_INVALIDA` quando proibida.

## Feche

- Atualize o diagrama e a tabela de estados do `README.md`.
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
