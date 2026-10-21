package com.example.processo.core.motor.application.service;

import com.example.processo.core.motor.application.port.out.Numeracao;
import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.SituacaoCadastral;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.motor.application.port.spi.Segmento;
import com.example.processo.core.motor.domain.event.ProcessoCriado;
import com.example.processo.core.motor.domain.exception.ConflitoDeSituacaoException;
import com.example.processo.core.motor.domain.exception.ProcessoNaoEncontradoException;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.rule.RegrasComuns;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Motor único: o mesmo fluxo de criação e o mesmo ciclo de vida para todos os segmentos. O que varia (dados,
 * regras, prefixo, gravação, decisão do processamento) vem do {@link Segmento}; os estados e as transições são do
 * motor, pelo padrão State ({@code domain.state}). É o ponto de entrada do core: o REST o injeta direto.
 *
 * <pre>
 * criar:     regras comuns → regras do segmento → situação cadastral (externo)  | fora de transação
 *            → número → abrir → segmento grava → publica ProcessoCriado          | transação curta
 * processar: carregar → ABERTO → EM_PROCESSAMENTO                                 | transação curta
 *            → segmento avalia                                                    | fora de transação
 *            → CONCLUIDO ou RECUSADO                                              | transação curta
 * cancelar:  carregar → ABERTO → CANCELADO                                        | transação curta
 * </pre>
 *
 * As trocas de situação são compare-and-set: se outra requisição mudou o processo no meio, falha com
 * {@link ConflitoDeSituacaoException} em vez de sobrescrever.
 */
@ApplicationScoped
public class MotorDeProcessamento {

    private final Map<Class<?>, Segmento<?>> porTipoDosDados = new HashMap<>();
    private final Map<String, Segmento<?>> porCodigo = new HashMap<>();
    private final SituacaoCadastral situacaoCadastral;
    private final Numeracao numeracao;
    private final Processos processos;
    private final Transacoes transacoes;
    private final Clock relogio;
    private final Consumer<ProcessoCriado> publicar;

    @Inject
    MotorDeProcessamento(@Any Instance<Segmento<?>> segmentos, SituacaoCadastral situacaoCadastral,
                         Numeracao numeracao, Processos processos, Transacoes transacoes, Clock relogio,
                         Event<ProcessoCriado> eventos) {
        this(segmentos, situacaoCadastral, numeracao, processos, transacoes, relogio, eventos::fire);
    }

    /** Sem CDI, para os testes: segmentos e publicação passados diretamente. */
    public MotorDeProcessamento(Iterable<? extends Segmento<?>> segmentos, SituacaoCadastral situacaoCadastral,
                                Numeracao numeracao, Processos processos, Transacoes transacoes, Clock relogio,
                                Consumer<ProcessoCriado> publicar) {
        for (var segmento : segmentos) {
            if (porCodigo.put(segmento.codigo(), segmento) != null
                    || porTipoDosDados.put(segmento.tipoDosDados(), segmento) != null) {
                throw new IllegalStateException("Segmento registrado duas vezes: " + segmento.codigo());
            }
        }
        this.situacaoCadastral = situacaoCadastral;
        this.numeracao = numeracao;
        this.processos = processos;
        this.transacoes = transacoes;
        this.relogio = relogio;
        this.publicar = publicar;
    }

    public <D extends DadosDoSegmento> Processo<D> criar(Solicitacao<D> solicitacao) {
        var segmento = segmentoDe(solicitacao.dados());
        // A chamada ao sistema externo vem por último: só acontece se a solicitação passou em todas as outras regras
        RegrasComuns.<D>todas().e(segmento.regras()).e(this::cadastroRegular).exigir(solicitacao);

        return transacoes.executar(() -> {
            var processo = Processo.abrir(numero(segmento), segmento.codigo(), solicitacao, LocalDate.now(relogio));
            segmento.salvar(processo);
            // Dentro da transação: quem observa com AFTER_SUCCESS (a impressão) só roda depois do commit
            publicar.accept(new ProcessoCriado(processo));
            return processo;
        });
    }

    public Processo<?> processar(UUID id) {
        return processar(segmentoDoProcesso(id), id);
    }

    public Processo<?> cancelar(UUID id) {
        return cancelar(segmentoDoProcesso(id), id);
    }

    private <D extends DadosDoSegmento> Processo<D> processar(Segmento<D> segmento, UUID id) {
        var aberto = carregar(segmento, id);
        var emProcessamento = mudar(aberto, aberto.iniciarProcessamento());

        var avaliacao = segmento.avaliar(emProcessamento);
        return mudar(emProcessamento, avaliacao.aprovada()
                ? emProcessamento.concluir()
                : emProcessamento.recusar(avaliacao.motivo()));
    }

    private <D extends DadosDoSegmento> Processo<D> cancelar(Segmento<D> segmento, UUID id) {
        var processo = carregar(segmento, id);
        return mudar(processo, processo.cancelar());
    }

    /** Grava a nova situação só se o processo ainda estiver na anterior. */
    private <D extends DadosDoSegmento> Processo<D> mudar(Processo<D> antes, Processo<D> depois) {
        boolean mudou = transacoes.executar(() -> processos.mudarSituacao(
                depois.id(), antes.situacao(), depois.situacao(), depois.motivoRecusa()));
        if (!mudou) {
            throw new ConflitoDeSituacaoException(depois.numero());
        }
        return depois;
    }

    private <D extends DadosDoSegmento> Processo<D> carregar(Segmento<D> segmento, UUID id) {
        return segmento.carregar(id).orElseThrow(() -> new ProcessoNaoEncontradoException(id));
    }

    private Segmento<?> segmentoDoProcesso(UUID id) {
        var codigo = processos.segmentoDe(id).orElseThrow(() -> new ProcessoNaoEncontradoException(id));
        var segmento = porCodigo.get(codigo);
        if (segmento == null) {
            throw new IllegalStateException("Processo " + id + " é do segmento " + codigo + ", que não está registrado");
        }
        return segmento;
    }

    private Optional<String> cadastroRegular(Solicitacao<?> solicitacao) {
        return situacaoCadastral.regular(solicitacao.proponente().cpf())
                ? Optional.empty()
                : Optional.of("Situação cadastral irregular para o CPF " + solicitacao.proponente().cpf());
    }

    @SuppressWarnings("unchecked") // o mapa é indexado por tipoDosDados(): a chave garante o tipo do valor
    private <D extends DadosDoSegmento> Segmento<D> segmentoDe(D dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Solicitação sem dados do segmento");
        }
        var segmento = (Segmento<D>) porTipoDosDados.get(dados.getClass());
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
