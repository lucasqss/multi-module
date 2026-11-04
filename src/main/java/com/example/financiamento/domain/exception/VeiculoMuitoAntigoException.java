package com.example.financiamento.domain.exception;

/** Só para VEICULO: idade máxima do bem. */
public class VeiculoMuitoAntigoException extends RegraNegocioException {

    public static final String CODIGO = "VEICULO_MUITO_ANTIGO";

    public VeiculoMuitoAntigoException(int anoModelo, int idadeMaxima) {
        super(CODIGO, "Veículo ano %d passa da idade máxima de %d anos".formatted(anoModelo, idadeMaxima));
    }
}
