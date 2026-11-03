package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.Cenarios;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.exception.ContaDebitoIndevidaException;
import com.example.consorcio.domain.exception.ContaDebitoObrigatoriaException;
import com.example.consorcio.domain.exception.CotaComProcessoEmAndamentoException;
import com.example.consorcio.domain.exception.CotaNaoAceitaPagamentoException;
import com.example.consorcio.domain.exception.LimitePixExcedidoException;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.exception.ValorExcedeSaldoDevedorException;
import com.example.consorcio.domain.exception.ValorMinimoBoletoException;
import com.example.consorcio.domain.exception.ValorNaoPositivoException;
import com.example.consorcio.domain.exception.VencimentoAlemDoPrazoException;
import com.example.consorcio.domain.exception.VencimentoEmDiaNaoUtilException;
import com.example.consorcio.domain.exception.VencimentoNoPassadoException;
import com.example.consorcio.domain.specification.pagamento.RegrasProcessoPagamento;
import org.junit.jupiter.api.Test;

import static com.example.consorcio.domain.Cenarios.HOJE;
import static com.example.consorcio.domain.Cenarios.solicitacao;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cada regra testada isoladamente: parte-se de um cenário válido, quebra-se exatamente uma coisa
 * e verifica-se a exceção ESPECÍFICA esperada.
 */
class RegrasProcessoPagamentoTest {

    private static void assertViola(Class<? extends RegraNegocioException> esperada, Cenarios cenario) {
        assertThrows(esperada, () -> RegrasProcessoPagamento.ABERTURA.validar(cenario.build()));
    }

    private static void assertValido(Cenarios cenario) {
        assertDoesNotThrow(() -> RegrasProcessoPagamento.ABERTURA.validar(cenario.build()));
    }

    @Test
    void cenarioPadraoEhValido() {
        assertValido(solicitacao());
    }

    @Test
    void cotaCanceladaOuEncerradaNaoAceita() {
        assertViola(CotaNaoAceitaPagamentoException.class, solicitacao().situacao(SituacaoCota.CANCELADA));
        assertViola(CotaNaoAceitaPagamentoException.class, solicitacao().situacao(SituacaoCota.ENCERRADA));
    }

    @Test
    void cotaInadimplenteAceita() {
        assertValido(solicitacao().situacao(SituacaoCota.INADIMPLENTE));
    }

    @Test
    void valorZero() {
        assertViola(ValorNaoPositivoException.class, solicitacao().forma(FormaPagamento.PIX).valor("0"));
    }

    @Test
    void valorAcimaDoSaldoDevedor() {
        assertViola(ValorExcedeSaldoDevedorException.class, solicitacao().saldoDevedor("100.00").valor("100.01"));
    }

    @Test
    void boletoAbaixoDoMinimo() {
        assertViola(ValorMinimoBoletoException.class, solicitacao().forma(FormaPagamento.BOLETO).valor("9.99"));
        assertValido(solicitacao().forma(FormaPagamento.PIX).valor("9.99"));
    }

    @Test
    void pixAcimaDoLimite() {
        assertViola(LimitePixExcedidoException.class, solicitacao().forma(FormaPagamento.PIX).valor("5000.01"));
        assertValido(solicitacao().forma(FormaPagamento.BOLETO).valor("5000.01"));
    }

    @Test
    void vencimentoNoPassado() {
        assertViola(VencimentoNoPassadoException.class, solicitacao().vencimento(HOJE.minusDays(1)));
    }

    @Test
    void vencimentoAlemDoPrazo() {
        assertViola(VencimentoAlemDoPrazoException.class, solicitacao().vencimento(HOJE.plusDays(35))); // segunda
    }

    @Test
    void vencimentoNoFimDeSemana() {
        assertViola(VencimentoEmDiaNaoUtilException.class, solicitacao().vencimento(HOJE.plusDays(4))); // sábado
    }

    @Test
    void debitoSemConta() {
        assertViola(ContaDebitoObrigatoriaException.class, solicitacao().forma(FormaPagamento.DEBITO_EM_CONTA));
    }

    @Test
    void contaSemSerDebito() {
        assertViola(ContaDebitoIndevidaException.class, solicitacao().forma(FormaPagamento.PIX).conta("1234", "99"));
    }

    @Test
    void processoEmAndamentoSoBloqueiaAbertura() {
        var cenario = solicitacao().possuiEmAndamento(true);
        assertViola(CotaComProcessoEmAndamentoException.class, cenario);
        assertDoesNotThrow(() -> RegrasProcessoPagamento.ALTERACAO.validar(cenario.build()));
    }

    @Test
    void ordemDoCatalogoDefineQualViolacaoAparece() {
        // cota cancelada E valor zero: a regra da cota vem antes no catálogo
        assertViola(CotaNaoAceitaPagamentoException.class,
                solicitacao().situacao(SituacaoCota.CANCELADA).forma(FormaPagamento.PIX).valor("0"));
    }
}
