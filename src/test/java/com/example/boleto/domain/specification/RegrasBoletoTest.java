package com.example.boleto.domain.specification;

import com.example.boleto.domain.Cenarios;
import com.example.boleto.domain.exception.BoletoSemItensException;
import com.example.boleto.domain.exception.BoletoVencidoException;
import com.example.boleto.domain.exception.DataPagamentoFuturaException;
import com.example.boleto.domain.exception.DestinatarioRepetidoException;
import com.example.boleto.domain.exception.ItemComValorZeradoException;
import com.example.boleto.domain.exception.LimiteBoletosEmAbertoExcedidoException;
import com.example.boleto.domain.exception.LimitePessoaFisicaExcedidoException;
import com.example.boleto.domain.exception.QuantidadeMaximaDestinatariosExcedidaException;
import com.example.boleto.domain.exception.QuantidadeMaximaItensExcedidaException;
import com.example.boleto.domain.exception.RegraNegocioException;
import com.example.boleto.domain.exception.ValorAbaixoDoMinimoException;
import com.example.boleto.domain.exception.ValorAcimaDoMaximoException;
import com.example.boleto.domain.exception.ValorPagoDivergenteException;
import com.example.boleto.domain.exception.VencimentoAlemDoPrazoException;
import com.example.boleto.domain.exception.VencimentoEmDiaNaoUtilException;
import com.example.boleto.domain.exception.VencimentoNoPassadoException;
import com.example.boleto.domain.specification.boleto.DestinatariosDoBoleto;
import com.example.boleto.domain.specification.boleto.RecebimentoPagamento;
import com.example.boleto.domain.specification.boleto.RegrasBoleto;
import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Email;
import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.valueobject.Pagamento;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static com.example.boleto.domain.Cenarios.CNPJ;
import static com.example.boleto.domain.Cenarios.EMAIL_PAGADOR;
import static com.example.boleto.domain.Cenarios.HOJE;
import static com.example.boleto.domain.Cenarios.boleto;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cada regra testada isoladamente: parte-se de um cenário válido, quebra-se exatamente uma coisa
 * e verifica-se a exceção ESPECÍFICA esperada. Um @Nested por momento do catálogo.
 */
class RegrasBoletoTest {

    @Nested
    class SecaoItens {

        private ItensBoleto itens(int quantidade, String valor) {
            return new ItensBoleto(IntStream.range(0, quantidade)
                    .mapToObj(i -> new ItemBoleto("Item " + i, Dinheiro.de(valor))).toList());
        }

        @Test
        void validos() {
            assertDoesNotThrow(() -> RegrasBoleto.ITENS.validar(itens(20, "10.00")));
        }

        @Test
        void secaoVaziaEhPermitidaDuranteAEdicao() {
            assertDoesNotThrow(() -> RegrasBoleto.ITENS.validar(ItensBoleto.VAZIO));
        }

        @Test
        void quantidadeMaxima() {
            assertThrows(QuantidadeMaximaItensExcedidaException.class, () -> RegrasBoleto.ITENS.validar(itens(21, "10.00")));
        }

        @Test
        void itemComValorZerado() {
            assertThrows(ItemComValorZeradoException.class, () -> RegrasBoleto.ITENS.validar(itens(1, "0")));
        }
    }

    @Nested
    class SecaoDestinatarios {

        private DestinatariosDoBoleto destinatarios(String... emails) {
            return new DestinatariosDoBoleto(boleto().pagador(),
                    new Destinatarios(List.of(emails).stream().map(Email::new).toList()));
        }

        @Test
        void validos() {
            assertDoesNotThrow(() -> RegrasBoleto.DESTINATARIOS.validar(destinatarios("a@x.com", "b@x.com")));
        }

        @Test
        void quantidadeMaxima() {
            assertThrows(QuantidadeMaximaDestinatariosExcedidaException.class, () -> RegrasBoleto.DESTINATARIOS
                    .validar(destinatarios("a@x.com", "b@x.com", "c@x.com", "d@x.com", "e@x.com", "f@x.com")));
        }

        @Test
        void repetidoOuIgualAoPagador() {
            assertThrows(DestinatarioRepetidoException.class,
                    () -> RegrasBoleto.DESTINATARIOS.validar(destinatarios("a@x.com", "A@x.com")));
            assertThrows(DestinatarioRepetidoException.class,
                    () -> RegrasBoleto.DESTINATARIOS.validar(destinatarios(EMAIL_PAGADOR)));
        }
    }

    @Nested
    class Emissao {

        private void assertViola(Class<? extends RegraNegocioException> esperada, Cenarios cenario) {
            assertThrows(esperada, () -> RegrasBoleto.EMISSAO.validar(cenario.emissao()));
        }

        private void assertValido(Cenarios cenario) {
            assertDoesNotThrow(() -> RegrasBoleto.EMISSAO.validar(cenario.emissao()));
        }

        @Test
        void cenarioPadraoEhValido() {
            assertValido(boleto());
        }

        @Test
        void completudeExigeAoMenosUmItem() {
            assertViola(BoletoSemItensException.class, boleto().semItens());
        }

        @Test
        void valorMinimo() {
            assertViola(ValorAbaixoDoMinimoException.class, boleto().valor("4.99"));
            assertValido(boleto().valor("5.00"));
        }

        @Test
        void valorMaximo() {
            assertViola(ValorAcimaDoMaximoException.class, boleto().documento(CNPJ).valor("100000.01"));
        }

        @Test
        void limiteDePessoaFisicaSoValeParaCpf() {
            assertViola(LimitePessoaFisicaExcedidoException.class, boleto().valor("10000.01"));
            assertValido(boleto().documento(CNPJ).valor("10000.01"));
        }

        @Test
        void vencimentoNoPassado() {
            assertViola(VencimentoNoPassadoException.class, boleto().vencimento(HOJE.minusDays(1)));
        }

        @Test
        void vencimentoAlemDoPrazo() {
            assertViola(VencimentoAlemDoPrazoException.class, boleto().vencimento(HOJE.plusDays(62))); // segunda
        }

        @Test
        void vencimentoNoFimDeSemana() {
            assertViola(VencimentoEmDiaNaoUtilException.class, boleto().vencimento(HOJE.plusDays(4))); // sábado
        }

        @Test
        void limiteDeBoletosEmAberto() {
            assertViola(LimiteBoletosEmAbertoExcedidoException.class, boleto().boletosEmAberto(3));
            assertValido(boleto().boletosEmAberto(2));
        }

        @Test
        void ordemDoCatalogoDefineQualViolacaoAparece() {
            // sem itens E vencimento no passado: completude vem antes no catálogo
            assertViola(BoletoSemItensException.class, boleto().semItens().vencimento(HOJE.minusDays(1)));
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
