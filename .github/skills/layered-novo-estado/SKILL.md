---
name: layered-novo-estado
description: Cria ou altera a máquina de estados de uma entidade em projeto Java layered usando o State pattern (classe por estado, sealed, proíbe tudo por padrão). Use quando pedirem um novo status, uma nova transição, um ciclo de vida para uma entidade, ou "só pode X quando estiver em Y".
---

# Novo estado ou transição (State pattern)

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`), que mostra
> o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

Comportamento que depende do status **nunca** vira `if (status == ...)` ou `switch` em entidade ou
use case. Ele mora em classes de `domain/state/`.

## 0. Localize os papéis

Pelo **"Mapa do projeto"**, ou buscando: `abstract sealed class Estado` (base), o enum `Status<Agregado>`
em `domain/state` (rótulo, junto dos estados), `TransicaoEstadoInvalidaException`, e o teste com a tabela
de transições (procure `PERMITIDAS`). **Leia a base e um estado existente antes de escrever.**
Se a entidade ainda não tem estados, vá ao passo C.

## Modelo

- **Enum** `Status<Agregado>`: só o rótulo (persistência/API).
- **Base** `Estado<Agregado>`: `abstract sealed`, toda ação lança `TransicaoEstadoInvalidaException` (409).
- **Estados concretos**: `final`, singleton `INSTANCIA`, sobrescrevem **só** o permitido.
- Transições **retornam** o próximo estado; a entidade faz `estado = estado.acao()`. Sem setter.
- Ações que dependem do status sem mudá-lo: `validarEdicao()`, `validarExclusao()`.
- Consultas (`emAberto()`) moram nos estados; o repositório deriva queries delas.
- Auto-transição é válida (`return this`), ex.: reenviar algo sem mudar de estado.
- Transição pode depender de um dado do fato: `receberPagamento(boolean conforme)` escolhe o destino.

## A) Nova transição entre estados existentes

1. Base: novo método lançando `acaoNaoPermitida("<verbo>")`.
2. Estados que permitem: sobrescreva retornando o destino.
3. Entidade: método de negócio na ordem fixa: estado autoriza (409) → specs, se houver (422) → muda.
4. Use case + entrypoint: skill `layered-novo-caso-de-uso` (`POST /{id}/<substantivo>`).

**Ilustração** (`EstadoBoleto`, `EstadoEmAnalise`, `Boleto`):

```java
// base: proibido por padrão
public EstadoBoleto devolverPagamento() {
    throw acaoNaoPermitida("devolver pagamento de");
}

// EstadoEmAnalise: o único que permite
@Override
public EstadoBoleto devolverPagamento() {
    return EstadoDevolvido.INSTANCIA;
}

// Boleto: o estado decide; a entidade só guarda o resultado
public void devolverPagamento(Motivo motivo) {
    Objects.requireNonNull(motivo, "motivo é obrigatório");
    estado = estado.devolverPagamento();
    motivoDevolucao = motivo;
}
```

## B) Novo estado

1. Valor novo no enum (atualize o diagrama no Javadoc).
2. Classe `final` em `domain/state` (copie um estado existente parecido).
3. Inclua no `permits` da base e no `switch` de `de(status)`. O compilador acusa se faltar.
4. Sobrescreva, nos estados de origem, as transições que levam a ele.
5. Persistência: `@Enumerated(STRING)`; confira o tamanho da coluna.

## C) Primeira máquina de estados de uma entidade

Crie, para o seu agregado: o enum `Status<Agregado>`, a `TransicaoEstadoInvalidaException` (família
`ConflitoException`), a base `sealed` e um estado `final` por status. Na entidade: campo
`private Estado<Agregado> estado`, iniciado com `inicial()`, reconstruído com `de(status)` em
`reconstituir(...)`, e `getStatus()` devolvendo `estado.status()`.

**Ilustração** (resumo de `domain/state` do boleto):

```java
public enum StatusBoleto { EM_EDICAO, EMITIDO, NOTIFICADO, PAGO, EM_ANALISE, DEVOLVIDO, CANCELADO }

public abstract sealed class EstadoBoleto
        permits EstadoEmEdicao, EstadoEmitido, EstadoNotificado, EstadoPago,
                EstadoEmAnalise, EstadoDevolvido, EstadoCancelado {

    public abstract StatusBoleto status();

    public EstadoBoleto emitir() { throw acaoNaoPermitida("emitir"); }
    public EstadoBoleto cancelar() { throw acaoNaoPermitida("cancelar"); }
    public void validarEdicao() { throw acaoNaoPermitida("editar"); }
    public boolean emAberto() { return false; }
    // ... demais ações, todas proibidas por padrão

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    public static EstadoBoleto de(StatusBoleto status) {
        return switch (status) {           // não compila se faltar um estado
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case EMITIDO -> EstadoEmitido.INSTANCIA;
            // ...
        };
    }

    public static EstadoBoleto inicial() { return EstadoEmEdicao.INSTANCIA; }
}

public final class EstadoEmEdicao extends EstadoBoleto {
    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();
    private EstadoEmEdicao() { }
    @Override public StatusBoleto status() { return StatusBoleto.EM_EDICAO; }
    @Override public EstadoBoleto emitir() { return EstadoEmitido.INSTANCIA; }
    @Override public void validarEdicao() { /* permitido */ }
    @Override public void validarExclusao() { /* permitido */ }
}
```

## Testes (obrigatório)

Teste de **tabela**: para cada (status × ação), o destino esperado ou `TransicaoEstadoInvalidaException`.
Mais: teste da entidade (ciclo completo e ordem 409 antes de 422) e teste HTTP com `409` +
`TRANSICAO_ESTADO_INVALIDA`.

**Ilustração** (`EstadoBoletoTest`):

```java
private static final Map<StatusBoleto, Map<String, StatusBoleto>> PERMITIDAS = Map.of(
        EM_EDICAO, Map.of("emitir", EMITIDO),
        EMITIDO, Map.of("notificar", NOTIFICADO, "cancelar", CANCELADO,
                "pagamentoConforme", PAGO, "pagamentoDivergente", EM_ANALISE),
        EM_ANALISE, Map.of("aceitarPagamento", PAGO, "devolverPagamento", DEVOLVIDO),
        PAGO, Map.of(),
        // ...
);
// percorre StatusBoleto.values() × ACOES: o que não está em PERMITIDAS tem que lançar
```

## Feche

Diagrama/tabela de estados na documentação, skill `layered-revisao-arquitetura` e testes.
