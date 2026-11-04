package com.example.pagamento.domain.specification;

import com.example.pagamento.domain.Cenarios;
import com.example.pagamento.domain.exception.BeneficiariosDiferentesException;
import com.example.pagamento.domain.exception.BoletoRepetidoNoPagamentoException;
import com.example.pagamento.domain.exception.BoletoVenceAntesDoPagamentoException;
import com.example.pagamento.domain.exception.ContaDebitoNaoInformadaException;
import com.example.pagamento.domain.exception.DataPagamentoEmDiaNaoUtilException;
import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.exception.PagamentoSemBoletosException;
import com.example.pagamento.domain.exception.QuantidadeDeBoletosInvalidaException;
import com.example.pagamento.domain.exception.ValorTotalAcimaDoLimiteException;
import com.example.pagamento.domain.specification.pagamento.RegrasPagamento;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.pagamento.domain.Cenarios.HOJE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** As regras do catálogo, organizadas por ELO da cadeia de submissão. */
class RegrasPagamentoTest {

    private static void submeter(Cenarios c) {
        RegrasPagamento.SUBMISSAO.validar(c.submissao());
    }

    @Test
    void secaoBoletosNaoAceitaRepetido() {
        var b = Cenarios.boleto(1, "10.00", HOJE.plusDays(10), "Fornecedor A");
        assertThrows(BoletoRepetidoNoPagamentoException.class,
                () -> RegrasPagamento.BOLETOS.validar(new BoletosDoPagamento(List.of(b, b))));
    }

    @Nested
    class ComunsATodasAsModalidades {

        @Test
        void exigemBoletosContaEData() {
            assertThrows(PagamentoSemBoletosException.class, () -> submeter(Cenarios.lote().semBoletos()));
            assertThrows(ContaDebitoNaoInformadaException.class, () -> submeter(Cenarios.individual().semConta()));
        }

        @Test
        void dataPrecisaSerFuturaEmDiaUtilEAntesDoVencimento() {
            assertThrows(DataPagamentoNoPassadoException.class,
                    () -> submeter(Cenarios.individual().dataPagamento(HOJE.minusDays(1))));
            assertThrows(DataPagamentoEmDiaNaoUtilException.class,
                    () -> submeter(Cenarios.individual().dataPagamento(HOJE.plusDays(4)))); // sábado
            assertThrows(BoletoVenceAntesDoPagamentoException.class,
                    () -> submeter(Cenarios.individual().vencimentoDoBoleto(HOJE.plusDays(1))));
        }
    }

    @Nested
    class Individual {

        @Test
        void exatamenteUmBoleto() {
            assertDoesNotThrow(() -> submeter(Cenarios.individual()));
            assertThrows(QuantidadeDeBoletosInvalidaException.class,
                    () -> submeter(Cenarios.individual().comBoleto(2, "1.00", "Fornecedor A")));
        }
    }

    @Nested
    class Processo {

        @Test
        void ateVinteBoletosDoMesmoBeneficiario() {
            assertDoesNotThrow(() -> submeter(Cenarios.processo()));
            assertDoesNotThrow(() -> submeter(Cenarios.processo().comBoletos(20)));
            assertThrows(QuantidadeDeBoletosInvalidaException.class, () -> submeter(Cenarios.processo().comBoletos(21)));
            assertThrows(BeneficiariosDiferentesException.class,
                    () -> submeter(Cenarios.processo().comBoleto(3, "1.00", "Fornecedor B")));
        }
    }

    @Nested
    class Lote {

        @Test
        void deDoisACemBoletosDeQualquerBeneficiario() {
            assertDoesNotThrow(() -> submeter(Cenarios.lote()));
            assertThrows(QuantidadeDeBoletosInvalidaException.class, () -> submeter(Cenarios.lote().comBoletos(1)));
            assertThrows(QuantidadeDeBoletosInvalidaException.class, () -> submeter(Cenarios.lote().comBoletos(101)));
        }

        @Test
        void totalAteUmMilhao() {
            assertDoesNotThrow(() -> submeter(Cenarios.lote().semBoletos()
                    .comBoleto(1, "500000.00", "A").comBoleto(2, "500000.00", "B")));
            assertThrows(ValorTotalAcimaDoLimiteException.class, () -> submeter(Cenarios.lote().semBoletos()
                    .comBoleto(1, "500000.00", "A").comBoleto(2, "500000.01", "B")));
        }
    }
}
