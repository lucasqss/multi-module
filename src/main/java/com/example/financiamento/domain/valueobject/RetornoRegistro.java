package com.example.financiamento.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado do registro da garantia, informado pelo ÓRGÃO (cartório ou trânsito, externo). É um FATO.
 * {@code registrado = true}: a garantia foi registrada (com a identificação que o órgão gravou);
 * {@code false}: o órgão recusou (documentação, divergência cadastral...).
 */
public record RetornoRegistro(IdProtocolo idProtocolo, boolean registrado, String identificacaoRegistrada,
                              String numeroRegistro, String motivoRecusa, Instant momento) {

    public RetornoRegistro {
        Objects.requireNonNull(idProtocolo, "protocolo é obrigatório");
        Objects.requireNonNull(momento);
        if (registrado) {
            Objects.requireNonNull(identificacaoRegistrada, "identificação registrada é obrigatória quando registrado");
            Objects.requireNonNull(numeroRegistro, "número do registro é obrigatório quando registrado");
        }
    }
}
