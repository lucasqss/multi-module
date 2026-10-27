package com.example.processo;

import com.example.jpacore.SituacaoProcesso;
import com.example.jpacore.TipoImovel;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Os enums do domínio e os do jpa-core (colunas) são espelhos: o jpa-core não pode depender do core, então a
 * duplicação é de propósito. Este teste avisa quando eles divergem, em vez de um {@code valueOf} falhar em produção.
 * Fica no app porque é o único módulo que enxerga os dois.
 */
class EnumsEspelhadosTest {

    static List<String> nomes(Class<? extends Enum<?>> tipo) {
        return Arrays.stream(tipo.getEnumConstants()).map(Enum::name).toList();
    }

    @Test
    void situacao() {
        assertEquals(nomes(Situacao.class), nomes(SituacaoProcesso.class),
                "Alinhe Situacao (core), SituacaoProcesso (jpa-core) e o CHECK de PROCESSO.SITUACAO numa migration nova");
    }

    @Test
    void tipoDoImovel() {
        assertEquals(nomes(DadosImobiliarios.TipoImovel.class), nomes(TipoImovel.class),
                "Alinhe DadosImobiliarios.TipoImovel (core), TipoImovel (jpa-core) e o CHECK de PROCESSO_IMOBILIARIO.TIPO_IMOVEL");
    }
}
