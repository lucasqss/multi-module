package com.example.processo.core.segmento.imobiliario.domain;

import com.example.processo.core.motor.domain.Especificacao;
import com.example.processo.core.motor.domain.Solicitacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

import static com.example.processo.core.motor.domain.Especificacao.exige;

/**
 * Regras do segmento que dependem só da solicitação. A regra que consulta o banco (matrícula com processo
 * aberto) fica no {@code SegmentoImobiliario}, que tem o port.
 */
public final class RegrasImobiliarias {

    static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
            "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private RegrasImobiliarias() {
    }

    private static final Especificacao<Solicitacao<DadosImobiliarios>> MATRICULA = exige(
            s -> s.dados().matricula() != null && s.dados().matricula().matches("\\d{1,20}"),
            "Matrícula do imóvel deve ter de 1 a 20 dígitos");

    private static final Especificacao<Solicitacao<DadosImobiliarios>> UF = exige(
            s -> UFS.contains(s.dados().uf()), "UF do imóvel inválida");

    private static final Especificacao<Solicitacao<DadosImobiliarios>> TIPO = exige(
            s -> s.dados().tipoImovel() != null, "Tipo do imóvel é obrigatório");

    private static final Especificacao<Solicitacao<DadosImobiliarios>> AVALIACAO = exige(
            s -> s.dados().valorAvaliacao() != null && s.dados().valorAvaliacao().signum() > 0,
            "Valor de avaliação do imóvel deve ser maior que zero");

    /** O valor do processo não passa da fração da avaliação permitida para o tipo do imóvel. */
    private static final Especificacao<Solicitacao<DadosImobiliarios>> DENTRO_DO_LIMITE = s -> {
        var tipo = s.dados().tipoImovel();
        var limite = s.dados().valorAvaliacao().multiply(tipo.limiteDaAvaliacao).setScale(2, RoundingMode.DOWN);
        return s.valor().compareTo(limite) <= 0
                ? Optional.empty()
                : Optional.of("Valor excede " + percentual(tipo.limiteDaAvaliacao) + " da avaliação para imóvel "
                        + tipo.name().toLowerCase() + " (máximo " + limite.toPlainString() + ")");
    };

    public static final Especificacao<Solicitacao<DadosImobiliarios>> TODAS =
            MATRICULA.e(UF).e(TIPO).e(AVALIACAO).e(DENTRO_DO_LIMITE);

    private static String percentual(BigDecimal fracao) {
        return fracao.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }
}
