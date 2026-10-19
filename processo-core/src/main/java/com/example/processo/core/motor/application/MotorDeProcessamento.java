package com.example.processo.core.motor.application;

import com.example.processo.core.motor.domain.DadosDoSegmento;
import com.example.processo.core.motor.domain.Numeracao;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.ProcessoCriado;
import com.example.processo.core.motor.domain.RegrasComuns;
import com.example.processo.core.motor.domain.Segmento;
import com.example.processo.core.motor.domain.Solicitacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Motor único do "criar processo": o mesmo fluxo para todos os segmentos. O que varia (regras, prefixo e
 * gravação dos dados próprios) vem do {@link Segmento} escolhido pela classe dos dados da solicitação.
 *
 * <pre>
 * regras comuns → regras do segmento → número → abrir → segmento grava → publica ProcessoCriado
 * </pre>
 */
@ApplicationScoped
public class MotorDeProcessamento {

    private final Map<Class<?>, Segmento<?>> segmentos = new HashMap<>();
    private final Numeracao numeracao;
    private final Clock relogio;
    private final Consumer<ProcessoCriado> publicar;

    @Inject
    MotorDeProcessamento(@Any Instance<Segmento<?>> segmentos, Numeracao numeracao, Clock relogio,
                         Event<ProcessoCriado> eventos) {
        this(segmentos, numeracao, relogio, eventos::fire);
    }

    /** Sem CDI, para os testes: segmentos e publicação passados diretamente. */
    public MotorDeProcessamento(Iterable<? extends Segmento<?>> segmentos, Numeracao numeracao, Clock relogio,
                                Consumer<ProcessoCriado> publicar) {
        var codigos = new HashSet<String>();
        for (var segmento : segmentos) {
            if (!codigos.add(segmento.codigo()) || this.segmentos.put(segmento.tipoDosDados(), segmento) != null) {
                throw new IllegalStateException("Segmento registrado duas vezes: " + segmento.codigo());
            }
        }
        this.numeracao = numeracao;
        this.relogio = relogio;
        this.publicar = publicar;
    }

    @Transactional
    public <D extends DadosDoSegmento> Processo<D> criar(Solicitacao<D> solicitacao) {
        var segmento = segmentoDe(solicitacao.dados());
        RegrasComuns.<D>todas().e(segmento.regras()).exigir(solicitacao);

        var processo = Processo.abrir(numero(segmento), segmento.codigo(), solicitacao, LocalDate.now(relogio));
        segmento.salvar(processo);
        publicar.accept(new ProcessoCriado(processo));
        return processo;
    }

    @SuppressWarnings("unchecked") // o mapa é indexado por tipoDosDados(): a chave garante o tipo do valor
    private <D extends DadosDoSegmento> Segmento<D> segmentoDe(D dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Solicitação sem dados do segmento");
        }
        var segmento = (Segmento<D>) segmentos.get(dados.getClass());
        if (segmento == null) {
            throw new IllegalArgumentException("Nenhum segmento registrado para " + dados.getClass().getName());
        }
        return segmento;
    }

    /** IMO-2026-000042: prefixo do segmento, ano da abertura e o sequencial único. */
    private String numero(Segmento<?> segmento) {
        return "%s-%d-%06d".formatted(segmento.prefixo(), LocalDate.now(relogio).getYear(), numeracao.proximo());
    }
}
