package com.example.boleto.domain.specification;

import com.example.boleto.domain.Cenarios;
import com.example.boleto.domain.exception.BoletoVencidoException;
import com.example.boleto.domain.exception.DataPagamentoFuturaException;
import com.example.boleto.domain.exception.LimiteBoletosEmAbertoExcedidoException;
import com.example.boleto.domain.exception.LimitePessoaFisicaExcedidoException;
import com.example.boleto.domain.exception.RegraNegocioException;
import com.example.boleto.domain.exception.ValorAbaixoDoMinimoException;
import com.example.boleto.domain.exception.ValorAcimaDoMaximoException;
import com.example.boleto.domain.exception.ValorPagoDivergenteException;
import com.example.boleto.domain.exception.VencimentoAlemDoPrazoException;
import com.example.boleto.domain.exception.VencimentoEmDiaNaoUtilException;
import com.example.boleto.domain.exception.VencimentoNoPassadoException;
import com.example.boleto.domain.specification.boleto.RecebimentoPagamento;
import com.example.boleto.domain.specification.boleto.RegrasBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagamento;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.example.boleto.domain.Cenarios.CNPJ;
import static com.example.boleto.domain.Cenarios.HOJE;
import static com.example.boleto.domain.Cenarios.solicitacao;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cada regra testada isoladamente: parte-se de um cenário válido, quebra-se exatamente uma coisa
 * e verifica-se a exceção ESPECÍFICA esperada.
 */
class RegrasBoletoTest {

    @Nested
    class Emissao {

        private void assertViola(Class<? extends RegraNegocioException> esperada, Cenarios cenario) {
            assertThrows(esperada, () -> RegrasBoleto.EMISSAO.validar(cenario.build()));
        }

        private void assertValido(Cenarios cenario) {
            assertDoesNotThrow(() -> RegrasBoleto.EMISSAO.validar(cenario.build()));
        }

        @Test
        void cenarioPadraoEhValido() {
            assertValido(solicitacao());
        }

        @Test
        void valorMinimo() {
            assertViola(ValorAbaixoDoMinimoException.class, solicitacao().valor("4.99"));
            assertValido(solicitacao().valor("5.00"));
        }

        @Test
        void valorMaximo() {
            assertViola(ValorAcimaDoMaximoException.class, solicitacao().documento(CNPJ).valor("100000.01"));
        }

        @Test
        void limiteDePessoaFisicaSoValeParaCpf() {
            assertViola(LimitePessoaFisicaExcedidoException.class, solicitacao().valor("10000.01"));
            assertValido(solicitacao().documento(CNPJ).valor("10000.01"));
        }

        @Test
        void vencimentoNoPassado() {
            assertViola(VencimentoNoPassadoException.class, solicitacao().vencimento(HOJE.minusDays(1)));
        }

        @Test
        void vencimentoAlemDoPrazo() {
            assertViola(VencimentoAlemDoPrazoException.class, solicitacao().vencimento(HOJE.plusDays(62))); // segunda
        }

        @Test
        void vencimentoNoFimDeSemana() {
            assertViola(VencimentoEmDiaNaoUtilException.class, solicitacao().vencimento(HOJE.plusDays(4))); // sábado
        }

        @Test
        void limiteDeBoletosEmAbertoSoBloqueiaEmissao() {
            var cenario = solicitacao().boletosEmAberto(3);
            assertViola(LimiteBoletosEmAbertoExcedidoException.class, cenario);
            assertDoesNotThrow(() -> RegrasBoleto.ALTERACAO.validar(cenario.build()));
            assertValido(solicitacao().boletosEmAberto(2));
        }

        @Test
        void ordemDoCatalogoDefineQualViolacaoAparece() {
            // valor abaixo do mínimo E vencimento no passado: a regra de valor vem antes no catálogo
            assertViola(ValorAbaixoDoMinimoException.class, solicitacao().valor("1.00").vencimento(HOJE.minusDays(1)));
        }
    }

    @Nested
    class Recebimento {

        private static final LocalDate VENCIMENTO = HOJE.plusDays(5);

        private RecebimentoPagamento recebimento(String valorPago, LocalDate data) {
            return new RecebimentoPagamento(Dinheiro.de("500.00"), VENCIMENTO,
                    new Pagamento(Dinheiro.de(valorPago), data), HOJE);
        }

        @Test
        void pagamentoValido() {
            assertDoesNotThrow(() -> RegrasBoleto.RECEBIMENTO.validar(recebimento("500.00", HOJE)));
        }

        @Test
        void dataFutura() {
            assertThrows(DataPagamentoFuturaException.class,
                    () -> RegrasBoleto.RECEBIMENTO.validar(recebimento("500.00", HOJE.plusDays(1))));
        }

        @Test
        void aposOVencimento() {
            var recebimento = new RecebimentoPagamento(Dinheiro.de("500.00"), HOJE.minusDays(2),
                    new Pagamento(Dinheiro.de("500.00"), HOJE), HOJE);
            assertThrows(BoletoVencidoException.class, () -> RegrasBoleto.RECEBIMENTO.validar(recebimento));
        }

        @Test
        void valorParcialOuAMaior() {
            assertThrows(ValorPagoDivergenteException.class,
                    () -> RegrasBoleto.RECEBIMENTO.validar(recebimento("499.99", HOJE)));
            assertThrows(ValorPagoDivergenteException.class,
                    () -> RegrasBoleto.RECEBIMENTO.validar(recebimento("500.01", HOJE)));
        }
    }
}
