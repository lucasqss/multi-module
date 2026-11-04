package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.ValorTotalAcimaDoLimiteException;
import com.example.pagamento.domain.specification.RegraNegocio;
import com.example.pagamento.domain.specification.Specification;
import com.example.pagamento.domain.specification.ValidadorRegras;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.Dinheiro;

import static com.example.pagamento.domain.specification.Specification.quando;

/**
 * CATÁLOGO das regras do processo de pagamento, agrupadas pelo MOMENTO em que valem:
 * <ul>
 *   <li><b>Seção</b> ({@link #BOLETOS}): coerência da parte enviada, a cada salvamento em EM_EDICAO.</li>
 *   <li><b>Submissão</b> ({@link #SUBMISSAO}): completude, ao fim da edição.</li>
 *   <li><b>Aprovação</b> ({@link #APROVACAO}) e <b>alçada</b> ({@link #ALCADA_ATINGIDA}): a alçada não é erro,
 *       é uma DECISÃO (Specification pura, sem exceção) que o estado usa para escolher o destino.</li>
 *   <li><b>Conformidade do retorno</b> ({@link #CONFORMIDADE_RETORNO}): CLASSIFICA um fato do banco
 *       ({@code primeiraViolacao}); a regra não atendida vira motivo de análise, não erro.</li>
 * </ul>
 * A ORDEM das listas é a ordem de avaliação (fail-fast).
 */
public final class RegrasProcessoPagamento {

    public static final Dinheiro LIMITE_POR_PROCESSO = Dinheiro.de("500000.00");
    public static final Dinheiro LIMITE_UMA_APROVACAO = Dinheiro.de("10000.00");

    // ======================= seção (a cada salvamento, em EM_EDICAO) =======================

    public static final ValidadorRegras<BoletosDoProcesso> BOLETOS = ValidadorRegras.de(
            new QuantidadeMaximaBoletosSpec(),
            new BoletosSemRepeticaoSpec());

    // ======================= submissão (completude) =======================

    public static final RegraNegocio<SubmissaoProcesso> VALOR_TOTAL_DENTRO_DO_LIMITE = RegraNegocio.de(
            new ValorTotalAteSpec(LIMITE_POR_PROCESSO),
            s -> new ValorTotalAcimaDoLimiteException(LIMITE_POR_PROCESSO));

    public static final ValidadorRegras<SubmissaoProcesso> SUBMISSAO = ValidadorRegras.de(
            new ProcessoComBoletosSpec(),
            new ContaDebitoInformadaSpec(),
            new DataPagamentoInformadaSpec(),
            VALOR_TOTAL_DENTRO_DO_LIMITE,
            new DataPagamentoNaoRetroativaSpec(),
            new DataPagamentoEmDiaUtilSpec(),
            new BoletosVencemAposPagamentoSpec());

    // ======================= aprovação =======================

    public static final ValidadorRegras<AprovacaoProcesso> APROVACAO = ValidadorRegras.de(
            new AprovadorDiferenteDoSolicitanteSpec(),
            new AprovadorNaoRepetidoSpec());

    /** Alçada (fake): acima de R$ 10.000 exige duas aprovações; até isso, uma basta. É DECISÃO, não erro. */
    public static final Specification<AprovacaoProcesso> ALCADA_ATINGIDA =
            quando(new ValorAcimaDeSpec(LIMITE_UMA_APROVACAO)).entao(new AprovacoesNoMinimoSpec(2));

    // ======================= conformidade do retorno do banco (classifica FATO) =======================

    public static final ValidadorRegras<RetornoPagamento> CONFORMIDADE_RETORNO = ValidadorRegras.de(
            new ValorLiquidadoIgualAoTotalSpec());

    private RegrasProcessoPagamento() {
    }
}
