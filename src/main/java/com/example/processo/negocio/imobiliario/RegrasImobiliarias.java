package com.example.processo.negocio.imobiliario;

import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.negocio.processo.Especificacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

import static com.example.processo.negocio.processo.Especificacao.exige;

/**
 * Regras da modalidade que dependem só do pedido. A regra que consulta o banco (matrícula com processo em andamento)
 * fica na {@code ModalidadeImobiliaria}, que tem o repository.
 */
public final class RegrasImobiliarias {

    static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
            "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private RegrasImobiliarias() {
    }

    private static final Especificacao<ProcessoImobiliario> MATRICULA = exige(
            p -> p.getMatricula() != null && p.getMatricula().matches("\\d{1,20}"),
            "Matrícula do imóvel deve ter de 1 a 20 dígitos");

    private static final Especificacao<ProcessoImobiliario> UF = exige(
            p -> UFS.contains(p.getUf()), "UF do imóvel inválida");

    private static final Especificacao<ProcessoImobiliario> TIPO = exige(
            p -> p.getTipoImovel() != null, "Tipo do imóvel é obrigatório");

    private static final Especificacao<ProcessoImobiliario> AVALIACAO = exige(
            p -> p.getValorAvaliacao() != null && p.getValorAvaliacao().signum() > 0,
            "Valor de avaliação do imóvel deve ser maior que zero");

    /** O valor do processo não passa da fração da avaliação permitida para o tipo do imóvel. */
    private static final Especificacao<ProcessoImobiliario> DENTRO_DO_LIMITE = p -> {
        var tipo = p.getTipoImovel();
        var limite = p.getValorAvaliacao().multiply(tipo.limiteDaAvaliacao).setScale(2, RoundingMode.DOWN);
        return p.getValor().compareTo(limite) <= 0
                ? Optional.empty()
                : Optional.of("Valor excede " + percentual(tipo.limiteDaAvaliacao) + " da avaliação para imóvel "
                        + tipo.name().toLowerCase() + " (máximo " + limite.toPlainString() + ")");
    };

    public static final Especificacao<ProcessoImobiliario> TODAS =
            MATRICULA.e(UF).e(TIPO).e(AVALIACAO).e(DENTRO_DO_LIMITE);

    private static String percentual(BigDecimal fracao) {
        return fracao.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }
}
