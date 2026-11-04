package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.IdProtocoloInvalidoException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;
import java.util.UUID;

/**
 * Protocolo de um envio para registro da garantia. Somos NÓS que geramos
 * ({@code <id do financiamento>-<número do envio>}), então ele carrega o financiamento: qualquer
 * retorno do órgão, mesmo de um envio antigo, leva ao financiamento certo.
 */
public record IdProtocolo(UUID financiamento, int envio) {

    public IdProtocolo {
        Objects.requireNonNull(financiamento, "financiamento é obrigatório");
        if (envio < 1) {
            throw new IllegalArgumentException("envio começa em 1");
        }
    }

    /** Lê o formato que enviamos ao órgão. */
    public static IdProtocolo de(String valor) {
        try {
            var separador = valor.lastIndexOf('-');
            return new IdProtocolo(UUID.fromString(valor.substring(0, separador)),
                    Integer.parseInt(valor.substring(separador + 1)));
        } catch (RuntimeException e) { // nulo, sem '-', UUID ou número inválidos
            throw new IdProtocoloInvalidoException(valor);
        }
    }

    @Override
    public String toString() {
        return financiamento + "-" + envio;
    }

    /** Persistência: coluna texto no mesmo formato enviado ao órgão. */
    @Converter(autoApply = true)
    public static class Conversor implements AttributeConverter<IdProtocolo, String> {
        @Override public String convertToDatabaseColumn(IdProtocolo v) { return v == null ? null : v.toString(); }
        @Override public IdProtocolo convertToEntityAttribute(String v) { return v == null ? null : IdProtocolo.de(v); }
    }
}
