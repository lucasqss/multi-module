package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.CpfInvalidoException;
import com.example.financiamento.domain.exception.IdProtocoloInvalidoException;
import com.example.financiamento.domain.exception.IdentificacaoDoBemInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Value objects com regra de formato (400). */
class IdProtocoloTest {

    @Test
    void protocoloIdaEVoltaPeloFormatoEnviadoAoOrgao() {
        var id = new IdProtocolo(UUID.randomUUID(), 3);
        assertEquals(id, IdProtocolo.de(id.toString()));
        assertThrows(IdProtocoloInvalidoException.class, () -> IdProtocolo.de("xpto"));
        assertThrows(IdProtocoloInvalidoException.class, () -> IdProtocolo.de(UUID.randomUUID() + "-0"));
        assertThrows(IdProtocoloInvalidoException.class, () -> IdProtocolo.de(null));
    }

    @Test
    void identificacaoDoBemSegueOFormatoDoTipo() {
        assertEquals("ABC1D23", TipoFinanciamento.VEICULO.identificacaoValida("abc-1d23"));
        assertEquals("12345", TipoFinanciamento.IMOVEL.identificacaoValida("12.345"));
        assertThrows(IdentificacaoDoBemInvalidaException.class, () -> TipoFinanciamento.VEICULO.identificacaoValida("12345"));
        assertThrows(IdentificacaoDoBemInvalidaException.class, () -> TipoFinanciamento.IMOVEL.identificacaoValida("ABC1D23"));
    }

    @Test
    void cpfComOnzeDigitosNaoTodosIguais() {
        assertEquals("12345678901", new Cpf("123.456.789-01").valor());
        assertThrows(CpfInvalidoException.class, () -> new Cpf("1234"));
        assertThrows(CpfInvalidoException.class, () -> new Cpf("11111111111"));
    }
}
