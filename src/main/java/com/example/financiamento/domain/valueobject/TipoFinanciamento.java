package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.IdentificacaoDoBemInvalidaException;

import java.util.Locale;

/**
 * Tipo do financiamento, fixo na criação. Sabe o que é próprio do tipo e não é regra de negócio:
 * o formato da identificação do bem e onde a garantia é registrada. As REGRAS que variam por tipo
 * ficam nos elos da cadeia em {@code RegrasFinanciamento}.
 */
public enum TipoFinanciamento {

    /** Garantia: o imóvel, identificado pela matrícula; registro no cartório de imóveis. */
    IMOVEL("\\d{1,10}", "cartório de registro de imóveis"),

    /** Garantia: o veículo, identificado pela placa (Mercosul); gravame no órgão de trânsito. */
    VEICULO("[A-Z]{3}\\d[A-Z0-9]\\d{2}", "órgão de trânsito (gravame)");

    private final String formatoIdentificacao;
    private final String orgaoDeRegistro;

    TipoFinanciamento(String formatoIdentificacao, String orgaoDeRegistro) {
        this.formatoIdentificacao = formatoIdentificacao;
        this.orgaoDeRegistro = orgaoDeRegistro;
    }

    /** Normaliza (sem espaços/traços, maiúsculas) e valida a identificação do bem. */
    public String identificacaoValida(String identificacao) {
        var normalizada = identificacao == null ? "" : identificacao.replaceAll("[\\s.-]", "").toUpperCase(Locale.ROOT);
        if (!normalizada.matches(formatoIdentificacao)) {
            throw new IdentificacaoDoBemInvalidaException(this, identificacao);
        }
        return normalizada;
    }

    public String orgaoDeRegistro() {
        return orgaoDeRegistro;
    }
}
