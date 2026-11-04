package com.example.financiamento.domain.entity;

import com.example.financiamento.domain.Cenarios;
import com.example.financiamento.domain.exception.IdentificacaoRegistradaDivergenteException;
import com.example.financiamento.domain.exception.TransicaoEstadoInvalidaException;
import com.example.financiamento.domain.exception.ValorAcimaDoPercentualException;
import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.IdProtocolo;
import com.example.financiamento.domain.valueobject.Motivo;
import com.example.financiamento.domain.valueobject.RetornoRegistro;
import org.junit.jupiter.api.Test;

import static com.example.financiamento.domain.Cenarios.HOJE;
import static com.example.financiamento.domain.Cenarios.recusado;
import static com.example.financiamento.domain.Cenarios.registrado;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O agregado: ordem estado (409) -> regras (422) -> mudança, e o tratamento do fato externo. */
class FinanciamentoTest {

    @Test
    void rascunhoPodeFicarIncompletoMasNaoSubmeteInvalido() {
        var f = Cenarios.imovel().valor("450000.00").rascunho(); // aceito na edição

        assertThrows(ValorAcimaDoPercentualException.class, () -> f.submeter(HOJE));
        assertEquals(StatusFinanciamento.EM_EDICAO, f.getStatus()); // regra violada: nada mudou
    }

    @Test
    void statusBarraAntesDasRegras() {
        var f = Cenarios.veiculo().aprovado();
        assertThrows(TransicaoEstadoInvalidaException.class, () -> f.submeter(HOJE));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> f.salvarDocumentos(Cenarios.veiculo().documentos()));
    }

    @Test
    void contratacaoGeraProtocoloEContrataQuandoRegistraOBemCerto() {
        var f = Cenarios.imovel().emRegistro();
        assertEquals(new IdProtocolo(f.getId(), 1), f.getIdProtocolo().orElseThrow());

        f.registrarRetornoRegistro(registrado(f, "500"));

        assertEquals(StatusFinanciamento.CONTRATADO, f.getStatus());
        assertEquals("REG-1", f.getNumeroRegistro().orElseThrow());
    }

    @Test
    void retornoRepetidoEIgnorado() {
        var f = Cenarios.veiculo().emRegistro();
        var retorno = registrado(f, "ABC1D23");
        f.registrarRetornoRegistro(retorno);
        f.registrarRetornoRegistro(retorno); // o mesmo aviso de novo: não lança, nada muda (idempotente)
        assertEquals(StatusFinanciamento.CONTRATADO, f.getStatus());
    }

    @Test
    void registroDeOutroBemNaoERecusadoVaiParaPendencia() {
        var f = Cenarios.veiculo().emRegistro();

        f.registrarRetornoRegistro(registrado(f, "XYZ9Z99"));

        assertEquals(StatusFinanciamento.EM_PENDENCIA, f.getStatus());
        assertEquals(IdentificacaoRegistradaDivergenteException.CODIGO, f.getMotivoPendencia().orElseThrow().codigo());
    }

    @Test
    void registroRecusadoPodeSerReenviadoComNovoProtocolo() {
        var f = Cenarios.imovel().emRegistro();
        f.registrarRetornoRegistro(recusado(f));
        assertEquals(StatusFinanciamento.REGISTRO_RECUSADO, f.getStatus());

        var antigo = f.getIdProtocolo().orElseThrow();
        f.reenviarRegistro();

        assertEquals(StatusFinanciamento.EM_REGISTRO, f.getStatus());
        assertEquals(new IdProtocolo(f.getId(), 2), f.getIdProtocolo().orElseThrow());
        assertTrue(f.getMotivoRecusa().isEmpty());
        f.registrarRetornoRegistro(new RetornoRegistro(antigo, false, null, null, "atrasado", Cenarios.AGORA)); // antigo: ignorado
        assertEquals(StatusFinanciamento.EM_REGISTRO, f.getStatus());
    }

    @Test
    void cancelarGuardaOMotivo() {
        var f = Cenarios.veiculo().aprovado();
        f.cancelar(new Motivo("cliente desistiu"));
        assertEquals(StatusFinanciamento.CANCELADO, f.getStatus());
        assertEquals("cliente desistiu", f.getMotivoCancelamento().orElseThrow().texto());
    }
}
