---
name: layered-novo-estado
description: Cria ou altera a máquina de estados de uma entidade em projeto Java layered usando o State pattern (classe por estado, sealed, proíbe tudo por padrão). Use quando pedirem um novo status, uma nova transição, um ciclo de vida para uma entidade, ou "só pode X quando estiver em Y".
---

# Novo estado ou transição (State pattern)

Comportamento que depende do status **nunca** vira `if (status == ...)` ou `switch` em entidade ou
use case. Ele mora em classes de `domain/state/`.

## 0. Localize os papéis

Pelo **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou buscando:
`abstract sealed class Estado` (base), `enum Status` em `domain/state` (rótulo, junto dos estados),
`TransicaoEstadoInvalidaException`, e o teste com a tabela de transições (procure `PERMITIDAS`).
**Leia a base e um estado existente antes de escrever.** Se a entidade ainda não tem estados, vá ao passo C.

## Modelo

- **Enum** `StatusContrato`: só o rótulo (persistência/API).
- **Base** `EstadoContrato`: `abstract sealed`, toda ação lança `TransicaoEstadoInvalidaException` (409).
- **Estados concretos**: `final`, singleton `INSTANCIA`, sobrescrevem **só** o permitido.
- Transições **retornam** o próximo estado; a entidade faz `estado = estado.acao()`. Sem setter.
- Ações que dependem do status sem mudá-lo: `validarEdicao()`, `validarExclusao()`.
- Consultas (`emAberto()`, `finalizado()`) moram nos estados; o adapter deriva queries delas.
- Auto-transição é válida (`return this`), ex.: reenviar algo sem mudar de estado.

## A) Nova transição entre estados existentes

1. Base: novo método lançando `acaoNaoPermitida("<verbo>")`.
2. Estados que permitem: sobrescreva retornando o destino.
3. Entidade: método de negócio na ordem fixa:
   ```java
   public void aprovar(Motivo motivo) {
       var proximo = estado.aprovar();   // 1. estado autoriza (409)
       // 2. specs, se houver regra sobre os dados (422)
       estado = proximo;                 // 3. muda
       this.motivoAprovacao = motivo;
   }
   ```
4. Use case + entrypoint: skill `layered-novo-caso-de-uso` (`POST /{id}/aprovacao`).

## B) Novo estado

1. Valor novo no enum (atualize o diagrama no Javadoc).
2. Classe `final` em `domain/state` (copie um estado existente parecido).
3. Inclua no `permits` da base e no `switch` de `de(status)`. O compilador acusa se faltar.
4. Sobrescreva, nos estados de origem, as transições que levam a ele.
5. Persistência: `@Enumerated(STRING)`; confira o tamanho da coluna.

## C) Primeira máquina de estados de uma entidade

Exemplo com o agregado **Contrato**; troque pelo nome do seu (ex.: `EstadoPedido`, `StatusPedido`).

```java
// domain/state (o enum de status mora junto dos estados)
public enum StatusContrato { EM_EDICAO, ATIVO, ENCERRADO }

// domain/exception (se ainda não existir para este agregado)
public class TransicaoEstadoInvalidaException extends ConflitoException {
    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";
    public TransicaoEstadoInvalidaException(StatusContrato atual, String acao) {
        super(CODIGO, "Não é possível %s com status %s".formatted(acao, atual));
    }
}

// domain/state
public abstract sealed class EstadoContrato permits EstadoEmEdicao, EstadoAtivo, EstadoEncerrado {

    public abstract StatusContrato status();

    public EstadoContrato ativar() { throw acaoNaoPermitida("ativar"); }
    public EstadoContrato encerrar() { throw acaoNaoPermitida("encerrar"); }
    public void validarEdicao() { throw acaoNaoPermitida("editar"); }

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    public static EstadoContrato de(StatusContrato status) {
        return switch (status) {
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case ATIVO -> EstadoAtivo.INSTANCIA;
            case ENCERRADO -> EstadoEncerrado.INSTANCIA;
        };
    }

    public static EstadoContrato inicial() { return EstadoEmEdicao.INSTANCIA; }
}

public final class EstadoEmEdicao extends EstadoContrato {
    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();
    private EstadoEmEdicao() { }
    @Override public StatusContrato status() { return StatusContrato.EM_EDICAO; }
    @Override public EstadoContrato ativar() { return EstadoAtivo.INSTANCIA; }
    @Override public void validarEdicao() { /* permitido */ }
}
```

Na entidade: campo `private EstadoContrato estado`, iniciado com `inicial()`, reconstruído com
`de(status)` em `reconstituir(...)`, e `getStatus()` devolvendo `estado.status()`.

## Testes (obrigatório)

Teste de **tabela**: para cada (status × ação), o destino esperado ou `TransicaoEstadoInvalidaException`.

```java
private static final Map<StatusContrato, Map<String, StatusContrato>> PERMITIDAS = Map.of(
        EM_EDICAO, Map.of("ativar", ATIVO),
        ATIVO, Map.of("encerrar", ENCERRADO),
        ENCERRADO, Map.of());
// percorra StatusContrato.values() × ACOES e compare com PERMITIDAS
```

Mais: teste da entidade (ciclo completo e ordem 409 antes de 422) e teste HTTP com `409` +
`TRANSICAO_ESTADO_INVALIDA`.

## Feche

Diagrama/tabela de estados na documentação, skill `layered-revisao-arquitetura` e testes.
