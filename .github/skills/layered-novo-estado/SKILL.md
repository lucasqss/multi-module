---
name: layered-novo-estado
description: Cria ou altera a máquina de estados de uma entidade em projeto Java layered usando o State pattern (classe por estado, sealed, proíbe tudo por padrão). Use quando pedirem um novo status, uma nova transição, um ciclo de vida para uma entidade, ou "só pode X quando estiver em Y".
---

# Novo estado ou transição (State pattern)

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`): um
> processo de pagamento de boletos, que mostra o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
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
- Transição pode depender de uma avaliação: `registrarAprovacao(boolean alcadaAtingida)`,
  `receberRetornoBanco(boolean liquidado, boolean conforme)` (ilustração) escolhem o destino.

## A) Nova transição entre estados existentes

1. Base: novo método lançando `acaoNaoPermitida("<verbo>")`.
2. Estados que permitem: sobrescreva retornando o destino.
3. Entidade: método de negócio na ordem fixa: estado autoriza (409) → specs, se houver (422) → muda.
4. Use case + entrypoint: skill `layered-novo-caso-de-uso` (`POST /{id}/<substantivo>`).

**Ilustração** (aprovação com alçada, no processo de pagamento: `EstadoProcesso`,
`EstadoAguardandoAprovacao`, `ProcessoPagamento`). Repare na auto-transição quando ainda falta aprovação:

```java
// base: proibido por padrão
public EstadoProcesso registrarAprovacao(boolean alcadaAtingida) {
    throw acaoNaoPermitida("aprovar");
}

// EstadoAguardandoAprovacao: o único que permite; o resultado da alçada escolhe o destino
@Override
public EstadoProcesso registrarAprovacao(boolean alcadaAtingida) {
    return alcadaAtingida ? EstadoAprovado.INSTANCIA : this;
}

// ProcessoPagamento: estado (409) -> regras (422) -> muda
public void aprovar(Matricula aprovador, Instant agora) {
    var candidato = new AprovacaoProcesso(solicitante, aprovadores(), aprovador, getValorTotal());
    var proximo = estado.registrarAprovacao(RegrasProcessoPagamento.ALCADA_ATINGIDA.isSatisfiedBy(candidato));
    RegrasProcessoPagamento.APROVACAO.validar(candidato);
    aprovacoes.add(Aprovacao.de(aprovador, agora));
    estado = proximo;
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

**Ilustração** (resumo de `domain/state` do processo de pagamento):

```java
public enum StatusProcesso { EM_EDICAO, AGUARDANDO_APROVACAO, REJEITADO, APROVADO,
                             EM_PROCESSAMENTO, FALHOU, PAGO, EM_ANALISE, CANCELADO }

public abstract sealed class EstadoProcesso
        permits EstadoEmEdicao, EstadoAguardandoAprovacao, EstadoRejeitado, EstadoAprovado,
                EstadoEmProcessamento, EstadoFalhou, EstadoPago, EstadoEmAnalise, EstadoCancelado {

    public abstract StatusProcesso status();

    public EstadoProcesso submeter() { throw acaoNaoPermitida("submeter"); }
    public EstadoProcesso cancelar() { throw acaoNaoPermitida("cancelar"); }
    public void validarEdicao() { throw acaoNaoPermitida("editar"); }
    // ... demais ações (inclusive FATOS externos, ex.: receberRetornoBanco), todas proibidas por padrão

    protected final TransicaoEstadoInvalidaException acaoNaoPermitida(String acao) {
        return new TransicaoEstadoInvalidaException(status(), acao);
    }

    public static EstadoProcesso de(StatusProcesso status) {
        return switch (status) {           // não compila se faltar um estado
            case EM_EDICAO -> EstadoEmEdicao.INSTANCIA;
            case AGUARDANDO_APROVACAO -> EstadoAguardandoAprovacao.INSTANCIA;
            // ...
        };
    }

    public static EstadoProcesso inicial() { return EstadoEmEdicao.INSTANCIA; }
}

public final class EstadoEmEdicao extends EstadoProcesso {
    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();
    private EstadoEmEdicao() { }
    @Override public StatusProcesso status() { return StatusProcesso.EM_EDICAO; }
    @Override public EstadoProcesso submeter() { return EstadoAguardandoAprovacao.INSTANCIA; }
    @Override public void validarEdicao() { /* permitido */ }
    @Override public void validarExclusao() { /* permitido */ }
}
```

## Testes (obrigatório)

Teste de **tabela**: para cada (status × ação), o destino esperado ou `TransicaoEstadoInvalidaException`.
Mais: teste da entidade (ciclo completo e ordem 409 antes de 422) e teste HTTP com `409` +
`TRANSICAO_ESTADO_INVALIDA`.

**Ilustração** (`EstadoProcessoTest`):

```java
private static final Map<StatusProcesso, Map<String, StatusProcesso>> PERMITIDAS = Map.of(
        EM_EDICAO, Map.of("submeter", AGUARDANDO_APROVACAO, "cancelar", CANCELADO, ...),
        AGUARDANDO_APROVACAO, Map.of("aprovarSemAlcada", AGUARDANDO_APROVACAO, "aprovarComAlcada", APROVADO, ...),
        EM_PROCESSAMENTO, Map.of("retornoLiquidadoConforme", PAGO, "retornoLiquidadoDivergente", EM_ANALISE,
                "retornoRecusado", FALHOU, "boletoCanceladoNoEmissor", EM_ANALISE),
        PAGO, Map.of("boletoCanceladoNoEmissor", EM_ANALISE),
        // ...
);
// percorre StatusProcesso.values() × ACOES: o que não está em PERMITIDAS tem que lançar
```

## Feche

Diagrama/tabela de estados na documentação, skill `layered-revisao-arquitetura` e testes.
