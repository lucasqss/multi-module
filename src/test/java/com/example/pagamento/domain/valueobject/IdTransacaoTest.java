package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.IdTransacaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdTransacaoTest {

    @Test
    void idaEVoltaPeloFormatoEnviadoAoBanco() {
        var id = new IdTransacao(UUID.randomUUID(), 3);
        assertEquals(id, IdTransacao.de(id.toString()));
    }

    @Test
    void formatoInvalido() {
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de("xpto"));
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de(UUID.randomUUID() + "-0"));
        assertThrows(IdTransacaoInvalidaException.class, () -> IdTransacao.de(null));
    }
}
