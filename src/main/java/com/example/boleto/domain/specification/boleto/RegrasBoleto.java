package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.LimitePessoaFisicaExcedidoException;
import com.example.boleto.domain.exception.ValorAbaixoDoMinimoException;
import com.example.boleto.domain.exception.ValorAcimaDoMaximoException;
import com.example.boleto.domain.specification.RegraNegocio;
import com.example.boleto.domain.specification.ValidadorRegras;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.ItensBoleto;

import static com.example.boleto.domain.specification.Specification.quando;

/**
 * CATÁLOGO das regras do boleto: um só lugar para ver/alterar o que é validado.
 *
 * <p>As regras são agrupadas pelo MOMENTO em que valem:
 * <ul>
 *   <li><b>Seções</b> ({@link #ITENS}, {@link #DESTINATARIOS}): coerência da parte enviada, a cada
 *       salvamento em EM_EDICAO. O boleto como um todo pode continuar incompleto.</li>
 *   <li><b>Emissão</b> ({@link #EMISSAO}): completude e regras do boleto inteiro, só ao finalizar a edição.</li>
 *   <li><b>Conformidade do pagamento</b> ({@link #CONFORMIDADE_PAGAMENTO}): usado como CLASSIFICAÇÃO,
 *       não como validação. O pagamento já aconteceu no sistema externo (é um fato): a primeira regra
 *       não atendida vira o motivo da análise, em vez de um erro.</li>
 * </ul>
 *
 * <p>Para criar uma regra nova: crie a exceção específica (em {@code domain.exception}), a spec
 * (ou componha), e adicione-a no conjunto do momento certo. Nenhum use case precisa mudar.
 *
 * <p>A ORDEM das listas é a ordem de avaliação (fail-fast): a primeira regra violada é a que o cliente recebe.
 */
public final class RegrasBoleto {

    public static final Dinheiro VALOR_MINIMO = Dinheiro.de("5.00");
    public static final Dinheiro VALOR_MAXIMO = Dinheiro.de("100000.00");
    public static final Dinheiro LIMITE_PESSOA_FISICA = Dinheiro.de("10000.00");

    public static final RegraNegocio<EmissaoBoleto> VALOR_ACIMA_DO_MINIMO = RegraNegocio.de(
            new ValorMinimoSpec(VALOR_MINIMO),
            s -> new ValorAbaixoDoMinimoException(VALOR_MINIMO));

    public static final RegraNegocio<EmissaoBoleto> VALOR_ABAIXO_DO_MAXIMO = RegraNegocio.de(
            new ValorAteSpec(VALOR_MAXIMO),
            s -> new ValorAcimaDoMaximoException(VALOR_MAXIMO));

    /** Se o pagador é pessoa física, então valor <= R$ 10.000 (regra fake). */
    public static final RegraNegocio<EmissaoBoleto> LIMITE_PESSOA_FISICA_RESPEITADO = RegraNegocio.de(
            quando(new PagadorPessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PESSOA_FISICA)),
            s -> new LimitePessoaFisicaExcedidoException(LIMITE_PESSOA_FISICA));

    // ======================= seções (a cada salvamento, em EM_EDICAO) =======================

    public static final ValidadorRegras<ItensBoleto> ITENS = ValidadorRegras.de(
            new QuantidadeMaximaItensSpec(),
            new ItensComValorPositivoSpec());

    public static final ValidadorRegras<DestinatariosDoBoleto> DESTINATARIOS = ValidadorRegras.de(
            new QuantidadeMaximaDestinatariosSpec(),
            new DestinatariosSemRepeticaoSpec());

    // ======================= emissão (completude, ao finalizar a edição) =======================

    public static final ValidadorRegras<EmissaoBoleto> EMISSAO = ValidadorRegras.de(
            new BoletoComItensSpec(),
            VALOR_ACIMA_DO_MINIMO,
            VALOR_ABAIXO_DO_MAXIMO,
            LIMITE_PESSOA_FISICA_RESPEITADO,
            new VencimentoNaoRetroativoSpec(),
            new VencimentoDentroDoPrazoSpec(),
            new VencimentoEmDiaUtilSpec(),
            new LimiteBoletosEmAbertoSpec());

    // ======================= conformidade do pagamento (classifica um FATO) =======================

    public static final ValidadorRegras<RecebimentoPagamento> CONFORMIDADE_PAGAMENTO = ValidadorRegras.de(
            new BoletoEmAbertoSpec(),
            new DataPagamentoNaoFuturaSpec(),
            new PagamentoAteOVencimentoSpec(),
            new ValorPagoIgualAoBoletoSpec());

    private RegrasBoleto() {
    }
}
