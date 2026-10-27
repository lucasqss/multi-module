package com.example.processo.core.motor.domain.model;

import com.example.processo.core.motor.domain.state.Situacao;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Processo de qualquer segmento: os dados comuns ficam aqui e os do segmento em {@code dados}. Um processo
 * imobiliário é um {@code Processo<DadosImobiliarios>}.
 *
 * <p>É o Context do padrão State: guarda a {@link Situacao} e delega cada evento a ela, que decide se a transição
 * vale. Imutável: cada evento devolve uma cópia na nova situação.
 */
public record Processo<D extends DadosDoSegmento>(UUID id, String numero, String segmento, Proponente proponente,
                                                  Valor valor, Situacao situacao, String motivoRecusa,
                                                  LocalDate dataAbertura, D dados) {

    public static <D extends DadosDoSegmento> Processo<D> abrir(String numero, String segmento,
                                                                Solicitacao<D> solicitacao, LocalDate hoje) {
        return new Processo<>(UUID.randomUUID(), numero, segmento, solicitacao.proponente(), solicitacao.valor(),
                Situacao.ABERTO, null, hoje, solicitacao.dados());
    }

    public Processo<D> iniciarProcessamento() {
        return em(situacao.iniciarProcessamento(), null);
    }

    public Processo<D> concluir() {
        return em(situacao.concluir(), null);
    }

    public Processo<D> recusar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Recusa exige motivo");
        }
        return em(situacao.recusar(), motivo);
    }

    public Processo<D> cancelar() {
        return em(situacao.cancelar(), null);
    }

    private Processo<D> em(Situacao nova, String motivo) {
        return new Processo<>(id, numero, segmento, proponente, valor, nova, motivo, dataAbertura, dados);
    }
}
