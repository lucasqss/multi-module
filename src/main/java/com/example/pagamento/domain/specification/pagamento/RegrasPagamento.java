package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.BeneficiariosDiferentesException;
import com.example.pagamento.domain.exception.BoletoRepetidoNoPagamentoException;
import com.example.pagamento.domain.exception.ContaDebitoNaoInformadaException;
import com.example.pagamento.domain.exception.DataPagamentoEmDiaNaoUtilException;
import com.example.pagamento.domain.exception.DataPagamentoNaoInformadaException;
import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.exception.PagamentoSemBoletosException;
import com.example.pagamento.domain.exception.ValorLiquidadoDivergenteException;
import com.example.pagamento.domain.exception.ValorTotalAcimaDoLimiteException;
import com.example.pagamento.domain.specification.CadeiaValidacao;
import com.example.pagamento.domain.specification.RegraNegocio;
import com.example.pagamento.domain.specification.ValidadorRegras;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Modalidade;

import java.time.DayOfWeek;
import java.util.Set;

import static com.example.pagamento.domain.specification.RegraNegocio.de;

/**
 * CATÁLOGO das regras do pagamento — a "tabela de regras", lida de uma vez — agrupadas pelo MOMENTO em que valem:
 * <ul>
 *   <li>{@link #BOLETOS}: ao salvar a seção de boletos (o pagamento pode continuar incompleto);</li>
 *   <li>{@link #SUBMISSAO}: completude + regras por modalidade, numa {@link CadeiaValidacao}
 *       (Chain of Responsibility): o primeiro elo vale sempre; os outros, só para a sua modalidade;</li>
 *   <li>{@link #CONFORMIDADE_RETORNO}: CLASSIFICA o retorno do banco (fato), não recusa.</li>
 * </ul>
 * Regra simples = uma linha {@code de(condição, exceção)} aqui. Vira classe própria só se for parametrizada
 * ({@link QuantidadeDeBoletosSpec}) ou tiver lógica/mensagem própria ({@link BoletosVencemAposPagamentoSpec}).
 * A ordem é a ordem de avaliação (fail-fast).
 */
public final class RegrasPagamento {

    public static final Dinheiro LIMITE_DO_LOTE = Dinheiro.de("1000000.00");
    private static final Set<DayOfWeek> FIM_DE_SEMANA = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    // ======================= seção (a cada salvamento, em EM_EDICAO) =======================

    public static final ValidadorRegras<BoletosDoPagamento> BOLETOS = ValidadorRegras.de(
            de(b -> !b.possuiRepetidos(), b -> new BoletoRepetidoNoPagamentoException()));

    // ======================= submissão: Chain of Responsibility por modalidade =======================

    private static final ValidadorRegras<SubmissaoPagamento> COMUNS = ValidadorRegras.<SubmissaoPagamento>de(
            de(s -> !s.boletos().vazio(), s -> new PagamentoSemBoletosException()),
            de(s -> s.contaDebito() != null, s -> new ContaDebitoNaoInformadaException()),
            de(s -> s.dataPagamento() != null, s -> new DataPagamentoNaoInformadaException()),
            de(s -> !s.dataPagamento().isBefore(s.hoje()), s -> new DataPagamentoNoPassadoException(s.dataPagamento())),
            de(s -> !FIM_DE_SEMANA.contains(s.dataPagamento().getDayOfWeek()),
                    s -> new DataPagamentoEmDiaNaoUtilException(s.dataPagamento())),
            new BoletosVencemAposPagamentoSpec());

    public static final CadeiaValidacao<SubmissaoPagamento> SUBMISSAO = CadeiaValidacao
            .comecandoPor(COMUNS)
            .depois(s -> s.modalidade() == Modalidade.INDIVIDUAL, ValidadorRegras.de(
                    new QuantidadeDeBoletosSpec(1, 1)))
            .depois(s -> s.modalidade() == Modalidade.PROCESSO, ValidadorRegras.<SubmissaoPagamento>de(
                    new QuantidadeDeBoletosSpec(1, 20),
                    de(s -> s.boletos().mesmoBeneficiario(), s -> new BeneficiariosDiferentesException())))
            .depois(s -> s.modalidade() == Modalidade.LOTE, ValidadorRegras.<SubmissaoPagamento>de(
                    new QuantidadeDeBoletosSpec(2, 100),
                    de(s -> !s.boletos().total().maiorQue(LIMITE_DO_LOTE),
                            s -> new ValorTotalAcimaDoLimiteException(LIMITE_DO_LOTE))));

    // ======================= conformidade do retorno do banco (classifica FATO) =======================

    public static final ValidadorRegras<RetornoPagamento> CONFORMIDADE_RETORNO = ValidadorRegras.de(
            de(r -> !r.retorno().liquidado() || r.retorno().valorLiquidado().equals(r.valorTotal()),
                    r -> new ValorLiquidadoDivergenteException(r.retorno().valorLiquidado(), r.valorTotal())));

    private RegrasPagamento() {
    }
}
