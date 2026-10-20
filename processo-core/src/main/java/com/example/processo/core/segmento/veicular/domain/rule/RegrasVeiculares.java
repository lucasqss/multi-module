package com.example.processo.core.segmento.veicular.domain.rule;

import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.rule.Especificacao;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.example.processo.core.motor.domain.rule.Especificacao.exige;

/** Regras do segmento veicular na abertura. A idade do veículo é decidida no processamento. */
public final class RegrasVeiculares {

    /** Fração máxima da tabela FIPE que o valor do processo pode alcançar. */
    static final BigDecimal LIMITE_DA_FIPE = new BigDecimal("0.90");

    private RegrasVeiculares() {
    }

    /** Padrão antigo (ABC1234) ou Mercosul (ABC1D23). */
    private static final Especificacao<Solicitacao<DadosVeiculares>> PLACA = exige(
            s -> s.dados().placa() != null && s.dados().placa().matches("[A-Z]{3}\\d[A-Z0-9]\\d{2}"),
            "Placa inválida");

    private static final Especificacao<Solicitacao<DadosVeiculares>> ANO = exige(
            s -> s.dados().anoFabricacao() != null && s.dados().anoFabricacao() >= 1950,
            "Ano de fabricação inválido");

    private static final Especificacao<Solicitacao<DadosVeiculares>> FIPE = exige(
            s -> s.dados().valorFipe() != null && s.dados().valorFipe().signum() > 0,
            "Valor FIPE deve ser maior que zero");

    private static final Especificacao<Solicitacao<DadosVeiculares>> DENTRO_DO_LIMITE = s -> {
        var limite = s.dados().valorFipe().multiply(LIMITE_DA_FIPE).setScale(2, RoundingMode.DOWN);
        return s.valor().compareTo(limite) <= 0
                ? Optional.empty()
                : Optional.of("Valor excede 90% da FIPE (máximo " + limite.toPlainString() + ")");
    };

    public static final Especificacao<Solicitacao<DadosVeiculares>> TODAS = PLACA.e(ANO).e(FIPE).e(DENTRO_DO_LIMITE);
}
