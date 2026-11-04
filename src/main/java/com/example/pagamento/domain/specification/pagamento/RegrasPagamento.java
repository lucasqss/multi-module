package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.ValorTotalAcimaDoLimiteException;
import com.example.pagamento.domain.specification.CadeiaValidacao;
import com.example.pagamento.domain.specification.RegraNegocio;
import com.example.pagamento.domain.specification.ValidadorRegras;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Modalidade;

/**
 * CATÁLOGO das regras do pagamento, agrupadas pelo MOMENTO em que valem:
 * <ul>
 *   <li>{@link #BOLETOS}: ao salvar a seção de boletos (o pagamento pode continuar incompleto);</li>
 *   <li>{@link #SUBMISSAO}: completude + regras por modalidade, numa {@link CadeiaValidacao}
 *       (Chain of Responsibility): o primeiro elo vale sempre; os outros, só para a sua modalidade;</li>
 *   <li>{@link #CONFORMIDADE_RETORNO}: CLASSIFICA o retorno do banco (fato), não recusa.</li>
 * </ul>
 * A ordem é a ordem de avaliação (fail-fast).
 */
public final class RegrasPagamento {

    public static final Dinheiro LIMITE_DO_LOTE = Dinheiro.de("1000000.00");

    // ======================= seção (a cada salvamento, em EM_EDICAO) =======================

    public static final ValidadorRegras<BoletosDoPagamento> BOLETOS = ValidadorRegras.de(
            new BoletosSemRepeticaoSpec());

    // ======================= submissão: Chain of Responsibility por modalidade =======================

    private static final ValidadorRegras<SubmissaoPagamento> COMUNS = ValidadorRegras.de(
            new PagamentoComBoletosSpec(),
            new ContaDebitoInformadaSpec(),
            new DataPagamentoInformadaSpec(),
            new DataPagamentoNaoRetroativaSpec(),
            new DataPagamentoEmDiaUtilSpec(),
            new BoletosVencemAposPagamentoSpec());

    private static final RegraNegocio<SubmissaoPagamento> TOTAL_DENTRO_DO_LIMITE_DO_LOTE = RegraNegocio.de(
            s -> !s.boletos().total().maiorQue(LIMITE_DO_LOTE),
            s -> new ValorTotalAcimaDoLimiteException(LIMITE_DO_LOTE));

    public static final CadeiaValidacao<SubmissaoPagamento> SUBMISSAO = CadeiaValidacao
            .comecandoPor(COMUNS)
            .depois(s -> s.modalidade() == Modalidade.INDIVIDUAL, ValidadorRegras.de(
                    new QuantidadeDeBoletosSpec(1, 1)))
            .depois(s -> s.modalidade() == Modalidade.PROCESSO, ValidadorRegras.de(
                    new QuantidadeDeBoletosSpec(1, 20),
                    new MesmoBeneficiarioSpec()))
            .depois(s -> s.modalidade() == Modalidade.LOTE, ValidadorRegras.de(
                    new QuantidadeDeBoletosSpec(2, 100),
                    TOTAL_DENTRO_DO_LIMITE_DO_LOTE));

    // ======================= conformidade do retorno do banco (classifica FATO) =======================

    public static final ValidadorRegras<RetornoPagamento> CONFORMIDADE_RETORNO = ValidadorRegras.de(
            new ValorLiquidadoIgualAoTotalSpec());

    private RegrasPagamento() {
    }
}
