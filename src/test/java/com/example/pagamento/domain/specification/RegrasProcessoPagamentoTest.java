package com.example.pagamento.domain.specification;

import com.example.pagamento.domain.Cenarios;
import com.example.pagamento.domain.exception.AprovadorIgualAoSolicitanteException;
import com.example.pagamento.domain.exception.AprovadorRepetidoException;
import com.example.pagamento.domain.exception.BoletoRepetidoNoProcessoException;
import com.example.pagamento.domain.exception.BoletoVenceAntesDoPagamentoException;
import com.example.pagamento.domain.exception.ContaDebitoNaoInformadaException;
import com.example.pagamento.domain.exception.DataPagamentoEmDiaNaoUtilException;
import com.example.pagamento.domain.exception.DataPagamentoNaoInformadaException;
import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.exception.ProcessoSemBoletosException;
import com.example.pagamento.domain.exception.QuantidadeMaximaBoletosExcedidaException;
import com.example.pagamento.domain.exception.RegraNegocioException;
import com.example.pagamento.domain.exception.ValorLiquidadoDivergenteException;
import com.example.pagamento.domain.exception.ValorTotalAcimaDoLimiteException;
import com.example.pagamento.domain.specification.processo.AprovacaoProcesso;
import com.example.pagamento.domain.specification.processo.RegrasProcessoPagamento;
import com.example.pagamento.domain.specification.processo.RetornoPagamento;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Matricula;
import com.example.pagamento.domain.valueobject.RetornoBanco;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.example.pagamento.domain.Cenarios.AGORA;
import static com.example.pagamento.domain.Cenarios.APROVADOR_1;
import static com.example.pagamento.domain.Cenarios.APROVADOR_2;
import static com.example.pagamento.domain.Cenarios.HOJE;
import static com.example.pagamento.domain.Cenarios.SOLICITANTE;
import static com.example.pagamento.domain.Cenarios.boleto;
import static com.example.pagamento.domain.Cenarios.processo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cada regra testada isoladamente a partir de um cenário válido. Um @Nested por MOMENTO do catálogo. */
class RegrasProcessoPagamentoTest {

    @Nested
    class SecaoBoletos {

        @Test
        void quantidadeMaxima() {
            var vinte = new BoletosDoProcesso(IntStream.rangeClosed(1, 20).mapToObj(i -> boleto(i, "10.00", HOJE.plusDays(10))).toList());
            var vinteEUm = new BoletosDoProcesso(IntStream.rangeClosed(1, 21).mapToObj(i -> boleto(i, "10.00", HOJE.plusDays(10))).toList());
            assertDoesNotThrow(() -> RegrasProcessoPagamento.BOLETOS.validar(vinte));
            assertThrows(QuantidadeMaximaBoletosExcedidaException.class, () -> RegrasProcessoPagamento.BOLETOS.validar(vinteEUm));
        }

        @Test
        void boletoRepetido() {
            var repetidos = new BoletosDoProcesso(List.of(boleto(1, "10.00", HOJE), boleto(1, "10.00", HOJE)));
            assertThrows(BoletoRepetidoNoProcessoException.class, () -> RegrasProcessoPagamento.BOLETOS.validar(repetidos));
        }

        @Test
        void secaoVaziaEhPermitidaDuranteAEdicao() {
            assertDoesNotThrow(() -> RegrasProcessoPagamento.BOLETOS.validar(BoletosDoProcesso.NENHUM));
        }
    }

    @Nested
    class Submissao {

        private void assertViola(Class<? extends RegraNegocioException> esperada, Cenarios cenario) {
            assertThrows(esperada, () -> RegrasProcessoPagamento.SUBMISSAO.validar(cenario.submissao()));
        }

        @Test
        void cenarioPadraoEhValido() {
            assertDoesNotThrow(() -> RegrasProcessoPagamento.SUBMISSAO.validar(processo().submissao()));
        }

        @Test
        void completude() {
            assertViola(ProcessoSemBoletosException.class, processo().semBoletos());
            assertViola(ContaDebitoNaoInformadaException.class, processo().semConta());
            assertViola(DataPagamentoNaoInformadaException.class, processo().dataPagamento(null));
        }

        @Test
        void valorTotalAcimaDoLimite() {
            assertViola(ValorTotalAcimaDoLimiteException.class, processo().valor("500000.01"));
        }

        @Test
        void dataDePagamento() {
            assertViola(DataPagamentoNoPassadoException.class, processo().dataPagamento(HOJE.minusDays(1)));
            assertViola(DataPagamentoEmDiaNaoUtilException.class, processo().dataPagamento(HOJE.plusDays(4))); // sábado
        }

        @Test
        void boletoQueVenceAntesDaDataDePagamento() {
            assertViola(BoletoVenceAntesDoPagamentoException.class, processo().vencimentoDoBoleto(HOJE.plusDays(2)));
        }

        @Test
        void ordemDoCatalogo_completudeAntesDoDetalhe() {
            assertViola(ProcessoSemBoletosException.class, processo().semBoletos().dataPagamento(HOJE.minusDays(1)));
        }
    }

    @Nested
    class AprovacaoEAlcada {

        private AprovacaoProcesso candidato(List<Matricula> anteriores, Matricula novo, String valor) {
            return new AprovacaoProcesso(SOLICITANTE, anteriores, novo, Dinheiro.de(valor));
        }

        @Test
        void segregacaoDeFuncoes() {
            assertThrows(AprovadorIgualAoSolicitanteException.class,
                    () -> RegrasProcessoPagamento.APROVACAO.validar(candidato(List.of(), SOLICITANTE, "100.00")));
        }

        @Test
        void aprovadorRepetido() {
            assertThrows(AprovadorRepetidoException.class,
                    () -> RegrasProcessoPagamento.APROVACAO.validar(candidato(List.of(APROVADOR_1), APROVADOR_1, "100.00")));
        }

        /** A alçada é DECISÃO (Specification pura), não erro. */
        @Test
        void alcada() {
            assertTrue(RegrasProcessoPagamento.ALCADA_ATINGIDA.isSatisfiedBy(candidato(List.of(), APROVADOR_1, "10000.00")));
            assertFalse(RegrasProcessoPagamento.ALCADA_ATINGIDA.isSatisfiedBy(candidato(List.of(), APROVADOR_1, "10000.01")));
            assertTrue(RegrasProcessoPagamento.ALCADA_ATINGIDA.isSatisfiedBy(candidato(List.of(APROVADOR_1), APROVADOR_2, "10000.01")));
        }
    }

    @Nested
    class ConformidadeDoRetorno {

        private Optional<String> motivo(boolean liquidado, String valorLiquidado) {
            var retorno = new RetornoBanco("TX", liquidado, valorLiquidado == null ? null : Dinheiro.de(valorLiquidado),
                    "AUT", null, AGORA);
            return RegrasProcessoPagamento.CONFORMIDADE_RETORNO
                    .primeiraViolacao(new RetornoPagamento(Dinheiro.de("500.00"), retorno))
                    .map(RegraNegocioException::getCodigo);
        }

        @Test
        void liquidadoExatoEhConforme() {
            assertEquals(Optional.empty(), motivo(true, "500.00"));
        }

        /** FATO: a regra não lança; devolve o motivo da análise. */
        @Test
        void liquidadoDivergenteViraMotivo() {
            assertEquals(Optional.of(ValorLiquidadoDivergenteException.CODIGO), motivo(true, "499.00"));
        }

        @Test
        void recusaNaoEhAvaliadaPelaConformidade() {
            assertEquals(Optional.empty(), motivo(false, null));
        }
    }
}
