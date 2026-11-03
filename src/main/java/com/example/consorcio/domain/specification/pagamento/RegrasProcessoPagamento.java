package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.exception.ContaDebitoIndevidaException;
import com.example.consorcio.domain.exception.ContaDebitoObrigatoriaException;
import com.example.consorcio.domain.exception.LimitePixExcedidoException;
import com.example.consorcio.domain.exception.ValorMinimoBoletoException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;
import com.example.consorcio.domain.specification.ValidadorRegras;
import com.example.consorcio.domain.valueobject.Dinheiro;

import static com.example.consorcio.domain.specification.Specification.quando;

/**
 * CATÁLOGO das regras do processo de pagamento: um só lugar para ver/alterar o que é validado.
 *
 * <p>Regras simples moram em classes próprias (ex.: {@link ValorPositivoSpec}).
 * Regras condicionais são COMPOSTAS aqui a partir de blocos de construção.
 *
 * <p>Para criar uma regra nova: crie a exceção específica (em {@code domain.exception}), a spec
 * (ou componha), e adicione-a em {@link #COMUNS}, {@link #ABERTURA} ou {@link #ALTERACAO}.
 * Nenhum use case precisa mudar.
 *
 * <p>A ORDEM das listas é a ordem de avaliação (fail-fast): a primeira regra violada é a que o cliente recebe.
 */
public final class RegrasProcessoPagamento {

    public static final Dinheiro LIMITE_PIX = Dinheiro.de("5000.00");
    public static final Dinheiro MINIMO_BOLETO = Dinheiro.de("10.00");

    private static final FormaPagamentoEhSpec EH_PIX = new FormaPagamentoEhSpec(FormaPagamento.PIX);
    private static final FormaPagamentoEhSpec EH_BOLETO = new FormaPagamentoEhSpec(FormaPagamento.BOLETO);
    private static final FormaPagamentoEhSpec EH_DEBITO = new FormaPagamentoEhSpec(FormaPagamento.DEBITO_EM_CONTA);
    private static final PossuiContaDebitoSpec POSSUI_CONTA = new PossuiContaDebitoSpec();

    /** Se é PIX, então valor <= R$ 5.000 (regra fake). */
    public static final RegraNegocio<SolicitacaoPagamento> LIMITE_PIX_RESPEITADO = RegraNegocio.de(
            quando(EH_PIX).entao(new ValorAteSpec(LIMITE_PIX)),
            s -> new LimitePixExcedidoException(LIMITE_PIX));

    /** Se é boleto, então valor >= R$ 10 (regra fake). */
    public static final RegraNegocio<SolicitacaoPagamento> MINIMO_BOLETO_RESPEITADO = RegraNegocio.de(
            quando(EH_BOLETO).entao(new ValorMinimoSpec(MINIMO_BOLETO)),
            s -> new ValorMinimoBoletoException(MINIMO_BOLETO));

    /** Se é débito em conta, então a conta é obrigatória. */
    public static final RegraNegocio<SolicitacaoPagamento> CONTA_INFORMADA_PARA_DEBITO = RegraNegocio.de(
            quando(EH_DEBITO).entao(POSSUI_CONTA),
            s -> new ContaDebitoObrigatoriaException());

    /** Se tem conta, então tem que ser débito em conta (mesmos blocos, direção inversa). */
    public static final RegraNegocio<SolicitacaoPagamento> CONTA_SOMENTE_PARA_DEBITO = RegraNegocio.de(
            quando(POSSUI_CONTA).entao(EH_DEBITO),
            s -> new ContaDebitoIndevidaException());

    /** Regras que valem tanto para abrir quanto para alterar um processo. */
    public static final ValidadorRegras<SolicitacaoPagamento> COMUNS = ValidadorRegras.de(
            new CotaAceitaPagamentoSpec(),
            new ValorPositivoSpec(),
            new ValorDentroDoSaldoDevedorSpec(),
            MINIMO_BOLETO_RESPEITADO,
            LIMITE_PIX_RESPEITADO,
            new VencimentoNaoRetroativoSpec(),
            new VencimentoDentroDoPrazoSpec(),
            new VencimentoEmDiaUtilSpec(),
            CONTA_INFORMADA_PARA_DEBITO,
            CONTA_SOMENTE_PARA_DEBITO);

    public static final ValidadorRegras<SolicitacaoPagamento> ABERTURA = COMUNS.mais(
            new CotaSemProcessoEmAndamentoSpec());

    public static final ValidadorRegras<SolicitacaoPagamento> ALTERACAO = COMUNS;

    private RegrasProcessoPagamento() {
    }
}
