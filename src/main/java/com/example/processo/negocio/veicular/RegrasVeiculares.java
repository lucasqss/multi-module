package com.example.processo.negocio.veicular;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.negocio.processo.Especificacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.example.processo.negocio.processo.Especificacao.exige;

/** Regras da modalidade veicular na abertura. A idade do veículo é decidida no processamento. */
public final class RegrasVeiculares {

    /** Fração máxima da tabela FIPE que o valor do processo pode alcançar. */
    static final BigDecimal LIMITE_DA_FIPE = new BigDecimal("0.90");

    private RegrasVeiculares() {
    }

    /** Padrão antigo (ABC1234) ou Mercosul (ABC1D23). */
    private static final Especificacao<ProcessoVeicular> PLACA = exige(
            p -> p.getPlaca() != null && p.getPlaca().matches("[A-Z]{3}\\d[A-Z0-9]\\d{2}"),
            "Placa inválida");

    private static final Especificacao<ProcessoVeicular> ANO = exige(
            p -> p.getAnoFabricacao() != null && p.getAnoFabricacao() >= 1950,
            "Ano de fabricação inválido");

    private static final Especificacao<ProcessoVeicular> FIPE = exige(
            p -> p.getValorFipe() != null && p.getValorFipe().signum() > 0,
            "Valor FIPE deve ser maior que zero");

    private static final Especificacao<ProcessoVeicular> DENTRO_DO_LIMITE = p -> {
        var limite = p.getValorFipe().multiply(LIMITE_DA_FIPE).setScale(2, RoundingMode.DOWN);
        return p.getValor().compareTo(limite) <= 0
                ? Optional.empty()
                : Optional.of("Valor excede 90% da FIPE (máximo " + limite.toPlainString() + ")");
    };

    public static final Especificacao<ProcessoVeicular> TODAS = PLACA.e(ANO).e(FIPE).e(DENTRO_DO_LIMITE);
}
