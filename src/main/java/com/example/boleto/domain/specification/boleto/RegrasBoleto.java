package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.LimitePessoaFisicaExcedidoException;
import com.example.boleto.domain.exception.ValorAbaixoDoMinimoException;
import com.example.boleto.domain.exception.ValorAcimaDoMaximoException;
import com.example.boleto.domain.model.RecebimentoPagamento;
import com.example.boleto.domain.model.SolicitacaoBoleto;
import com.example.boleto.domain.specification.RegraNegocio;
import com.example.boleto.domain.specification.ValidadorRegras;
import com.example.boleto.domain.valueobject.Dinheiro;

import static com.example.boleto.domain.specification.Specification.quando;

/**
 * CATÁLOGO das regras do boleto: um só lugar para ver/alterar o que é validado.
 *
 * <p>Regras simples moram em classes próprias (ex.: {@link VencimentoEmDiaUtilSpec}).
 * Regras de valor são COMPOSTAS aqui a partir de blocos de construção.
 *
 * <p>Para criar uma regra nova: crie a exceção específica (em {@code domain.exception}), a spec
 * (ou componha), e adicione-a no conjunto certo. Nenhum use case precisa mudar.
 *
 * <p>A ORDEM das listas é a ordem de avaliação (fail-fast): a primeira regra violada é a que o cliente recebe.
 */
public final class RegrasBoleto {

    public static final Dinheiro VALOR_MINIMO = Dinheiro.de("5.00");
    public static final Dinheiro VALOR_MAXIMO = Dinheiro.de("100000.00");
    public static final Dinheiro LIMITE_PESSOA_FISICA = Dinheiro.de("10000.00");

    public static final RegraNegocio<SolicitacaoBoleto> VALOR_ACIMA_DO_MINIMO = RegraNegocio.de(
            new ValorMinimoSpec(VALOR_MINIMO),
            s -> new ValorAbaixoDoMinimoException(VALOR_MINIMO));

    public static final RegraNegocio<SolicitacaoBoleto> VALOR_ABAIXO_DO_MAXIMO = RegraNegocio.de(
            new ValorAteSpec(VALOR_MAXIMO),
            s -> new ValorAcimaDoMaximoException(VALOR_MAXIMO));

    /** Se o pagador é pessoa física, então valor <= R$ 10.000 (regra fake). */
    public static final RegraNegocio<SolicitacaoBoleto> LIMITE_PESSOA_FISICA_RESPEITADO = RegraNegocio.de(
            quando(new PagadorPessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PESSOA_FISICA)),
            s -> new LimitePessoaFisicaExcedidoException(LIMITE_PESSOA_FISICA));

    /** Regras que valem tanto para emitir quanto para alterar um boleto. */
    public static final ValidadorRegras<SolicitacaoBoleto> COMUNS = ValidadorRegras.de(
            VALOR_ACIMA_DO_MINIMO,
            VALOR_ABAIXO_DO_MAXIMO,
            LIMITE_PESSOA_FISICA_RESPEITADO,
            new VencimentoNaoRetroativoSpec(),
            new VencimentoDentroDoPrazoSpec(),
            new VencimentoEmDiaUtilSpec());

    public static final ValidadorRegras<SolicitacaoBoleto> EMISSAO = COMUNS.mais(
            new LimiteBoletosEmAbertoSpec());

    public static final ValidadorRegras<SolicitacaoBoleto> ALTERACAO = COMUNS;

    /** Regras do recebimento do pagamento informado pelo banco (outro tipo de candidato). */
    public static final ValidadorRegras<RecebimentoPagamento> RECEBIMENTO = ValidadorRegras.de(
            new DataPagamentoNaoFuturaSpec(),
            new PagamentoAteOVencimentoSpec(),
            new ValorPagoIgualAoBoletoSpec());

    private RegrasBoleto() {
    }
}
