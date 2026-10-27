package com.example.processo.core;

import com.example.processo.core.motor.application.port.out.GeradorDeDocumentos;
import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Fakes dos ports: o motor e os segmentos rodam sem Quarkus, sem banco e sem transação. Os de persistência passam
 * pelo mesmo contrato que os repositories reais ({@link PersistenciaDoProcessoContrato}), para não mentirem.
 */
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

    /** Sistema de PDF em memória: guarda cada processo pedido e, se mandado, falha como o sistema fora do ar. */
    public static class DocumentosEmMemoria implements GeradorDeDocumentos {

        public final List<Processo<?>> pedidos = new ArrayList<>();
        public boolean simularFalha;

        @Override
        public void gerarAbertura(Processo<?> processo) {
            pedidos.add(processo);
            if (simularFalha) {
                throw new SistemaExternoIndisponivelException("de PDF", new RuntimeException("fora do ar"));
            }
        }
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

    /** O port do segmento imobiliário sobre o mesmo mapa do {@link BancoEmMemoria}, como a tabela filha sobre a pai. */
    public static class ProcessosImobiliariosEmMemoria implements ProcessosImobiliarios {

        private final BancoEmMemoria banco;

        public ProcessosImobiliariosEmMemoria(BancoEmMemoria banco) {
            this.banco = banco;
        }

        @Override
        public boolean existeEmAndamentoParaMatricula(String matricula, String uf) {
            return gravados().stream().anyMatch(p -> (p.situacao() == Situacao.ABERTO
                    || p.situacao() == Situacao.EM_PROCESSAMENTO)
                    && p.dados().matricula().equals(matricula) && p.dados().uf().equals(uf));
        }

        @Override
        public void salvar(Processo<DadosImobiliarios> processo) {
            banco.processos.put(processo.id(), processo);
        }

        @Override
        public Optional<Processo<DadosImobiliarios>> porId(UUID id) {
            return gravados().stream().filter(p -> p.id().equals(id)).findFirst();
        }

        /** Os processos imobiliários gravados, na ordem de gravação. */
        @SuppressWarnings("unchecked")
        public List<Processo<DadosImobiliarios>> gravados() {
            return banco.processos.values().stream()
                    .filter(p -> p.dados() instanceof DadosImobiliarios)
                    .map(p -> (Processo<DadosImobiliarios>) p)
                    .toList();
        }
    }
}
