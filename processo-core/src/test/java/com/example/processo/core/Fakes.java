package com.example.processo.core;

import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.state.Situacao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Fakes dos ports do motor: o motor roda sem Quarkus, sem banco e sem transação. */
public final class Fakes {

    private Fakes() {
    }

    public static Transacoes semTransacao() {
        return new Transacoes() {
            @Override
            public <T> T executar(Supplier<T> trabalho) {
                return trabalho.get();
            }
        };
    }

    /**
     * Banco em memória: os segmentos de teste gravam e leem aqui, e o motor troca a situação pelo port
     * {@link Processos}, como as duas tabelas (pai e filha) fariam.
     */
    public static class BancoEmMemoria implements Processos {

        public final Map<UUID, Processo<?>> processos = new LinkedHashMap<>();
        /** Faz o compare-and-set falhar, como se outra requisição tivesse mudado o processo antes. */
        public boolean simularConflito;

        @Override
        public Optional<String> segmentoDe(UUID id) {
            return Optional.ofNullable(processos.get(id)).map(Processo::segmento);
        }

        @Override
        public boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
            var atual = processos.get(id);
            if (simularConflito || atual == null || atual.situacao() != de) {
                return false;
            }
            processos.put(id, comSituacao(atual, para, motivoRecusa));
            return true;
        }

        private static <D extends DadosDoSegmento> Processo<D> comSituacao(Processo<D> p, Situacao situacao,
                                                                           String motivo) {
            return new Processo<>(p.id(), p.numero(), p.segmento(), p.proponente(), p.valor(), situacao, motivo,
                    p.dataAbertura(), p.dados());
        }
    }
}
