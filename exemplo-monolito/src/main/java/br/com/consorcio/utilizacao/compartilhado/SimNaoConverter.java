package br.com.consorcio.utilizacao.compartilhado;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Colunas CHAR(1) 'S'/'N' do Oracle como boolean. */
@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        return "S".equals(valor);
    }
}
