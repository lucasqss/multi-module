package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.IdTransacaoInvalidaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdTransacaoTest {

    @Test
    void idaEVoltaPeloFormatoEnviadoAoBanco() {
        var id = new IdTransacao(PagamentoId.novo(), 3);
        assertEquals(id, IdTransacao.de(id.toString()));
    }

    @Test
    void formatoInvalido() {
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de("xpto"));
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de(PagamentoId.novo() + "-0"));
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de(null));
    }
}
