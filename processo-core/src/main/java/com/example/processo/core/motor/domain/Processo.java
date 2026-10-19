package com.example.processo.core.motor.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Processo de qualquer segmento: os dados comuns ficam aqui e os do segmento em {@code dados}. Um processo
 * imobiliário é um {@code Processo<DadosImobiliarios>}.
 */
public record Processo<D extends DadosDoSegmento>(UUID id, String numero, String segmento, Proponente proponente,
                                                  BigDecimal valor, Situacao situacao, LocalDate dataAbertura,
                                                  D dados) {

    public enum Situacao { ABERTO, CONCLUIDO, CANCELADO }

    public static <D extends DadosDoSegmento> Processo<D> abrir(String numero, String segmento,
                                                                Solicitacao<D> solicitacao, LocalDate hoje) {
        return new Processo<>(UUID.randomUUID(), numero, segmento, solicitacao.proponente(), solicitacao.valor(),
                Situacao.ABERTO, hoje, solicitacao.dados());
    }
}
