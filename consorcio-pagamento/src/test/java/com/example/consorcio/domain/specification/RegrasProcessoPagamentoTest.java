package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.Cenarios;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.exception.Violacao;
import com.example.consorcio.domain.specification.pagamento.RegrasProcessoPagamento;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.consorcio.domain.Cenarios.HOJE;
import static com.example.consorcio.domain.Cenarios.solicitacao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cada regra é testada isoladamente via catálogo: parte-se de um cenário válido
 * e quebra-se exatamente uma coisa.
 */
class RegrasProcessoPagamentoTest {

    private List<String> violacoesDeAbertura(Cenarios cenario) {
        return RegrasProcessoPagamento.ABERTURA.avaliar(cenario.build()).stream().map(Violacao::codigo).toList();
    }

    private void assertViola(String codigo, Cenarios cenario) {
        assertEquals(List.of(codigo), violacoesDeAbertura(cenario));
    }

    @Test
    void cenarioPadraoEhValido() {
        assertTrue(violacoesDeAbertura(solicitacao()).isEmpty());
    }

    @Test
    void cotaCanceladaOuEncerradaNaoAceita() {
        assertViola("COTA_NAO_ACEITA_PAGAMENTO", solicitacao().situacao(SituacaoCota.CANCELADA));
        assertViola("COTA_NAO_ACEITA_PAGAMENTO", solicitacao().situacao(SituacaoCota.ENCERRADA));
    }

    @Test
    void cotaInadimplenteAceita() {
        assertTrue(violacoesDeAbertura(solicitacao().situacao(SituacaoCota.INADIMPLENTE)).isEmpty());
    }

    @Test
    void valorZero() {
        assertViola("VALOR_NAO_POSITIVO", solicitacao().forma(FormaPagamento.PIX).valor("0"));
    }

    @Test
    void valorAcimaDoSaldoDevedor() {
        assertViola("VALOR_EXCEDE_SALDO_DEVEDOR", solicitacao().saldoDevedor("100.00").valor("100.01"));
    }

    @Test
    void boletoAbaixoDoMinimo() {
        assertViola("VALOR_MINIMO_BOLETO", solicitacao().forma(FormaPagamento.BOLETO).valor("9.99"));
        assertTrue(violacoesDeAbertura(solicitacao().forma(FormaPagamento.PIX).valor("9.99")).isEmpty());
    }

    @Test
    void pixAcimaDoLimite() {
        assertViola("LIMITE_PIX_EXCEDIDO", solicitacao().forma(FormaPagamento.PIX).valor("5000.01"));
        assertTrue(violacoesDeAbertura(solicitacao().forma(FormaPagamento.BOLETO).valor("5000.01")).isEmpty());
    }

    @Test
    void vencimentoNoPassado() {
        assertViola("VENCIMENTO_NO_PASSADO", solicitacao().vencimento(HOJE.minusDays(1)));
    }

    @Test
    void vencimentoAlemDoPrazo() {
        assertViola("VENCIMENTO_ALEM_DO_PRAZO", solicitacao().vencimento(HOJE.plusDays(35))); // segunda
    }

    @Test
    void vencimentoNoFimDeSemana() {
        assertViola("VENCIMENTO_EM_DIA_NAO_UTIL", solicitacao().vencimento(HOJE.plusDays(4))); // sábado
    }

    @Test
    void debitoSemConta() {
        assertViola("CONTA_DEBITO_OBRIGATORIA", solicitacao().forma(FormaPagamento.DEBITO_EM_CONTA));
    }

    @Test
    void contaSemSerDebito() {
        assertViola("CONTA_DEBITO_INDEVIDA", solicitacao().forma(FormaPagamento.PIX).conta("1234", "99"));
    }

    @Test
    void processoEmAndamentoSoBloqueiaAbertura() {
        var cenario = solicitacao().possuiEmAndamento(true);
        assertViola("COTA_COM_PROCESSO_EM_ANDAMENTO", cenario);
        assertTrue(RegrasProcessoPagamento.ALTERACAO.avaliar(cenario.build()).isEmpty());
    }

    @Test
    void acumulaVariasViolacoes() {
        var cenario = solicitacao().forma(FormaPagamento.PIX).valor("6000").conta("1234", "1")
                .vencimento(HOJE.plusDays(4));
        assertEquals(List.of("LIMITE_PIX_EXCEDIDO", "VENCIMENTO_EM_DIA_NAO_UTIL", "CONTA_DEBITO_INDEVIDA"),
                violacoesDeAbertura(cenario));
    }
}
