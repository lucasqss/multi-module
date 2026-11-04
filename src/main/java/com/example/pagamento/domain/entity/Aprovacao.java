package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.valueobject.Matricula;

import java.time.Instant;
import java.util.Objects;

/**
 * ENTIDADE INTERNA do {@link ProcessoPagamento}: tem identidade (o aprovador, único dentro do processo)
 * e só existe dentro dele. Não tem repositório nem use case próprios: é criada pelo processo.
 */
public final class Aprovacao {

    private final Matricula aprovador;
    private final Instant momento;

    private Aprovacao(Matricula aprovador, Instant momento) {
        this.aprovador = Objects.requireNonNull(aprovador);
        this.momento = Objects.requireNonNull(momento);
    }

    static Aprovacao de(Matricula aprovador, Instant momento) {
        return new Aprovacao(aprovador, momento);
    }

    public static Aprovacao reconstituir(Matricula aprovador, Instant momento) {
        return new Aprovacao(aprovador, momento);
    }

    public Matricula getAprovador() { return aprovador; }
    public Instant getMomento() { return momento; }

    /** Identidade dentro do agregado: o aprovador. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Aprovacao outra && aprovador.equals(outra.aprovador);
    }

    @Override
    public int hashCode() {
        return aprovador.hashCode();
    }
}
