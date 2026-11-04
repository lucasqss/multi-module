package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.exception.CondicoesNaoInformadasException;
import com.example.financiamento.domain.exception.DocumentoRepetidoException;
import com.example.financiamento.domain.exception.GarantiaNaoInformadaException;
import com.example.financiamento.domain.exception.IdentificacaoRegistradaDivergenteException;
import com.example.financiamento.domain.exception.ParcelaAcimaDaRendaException;
import com.example.financiamento.domain.exception.ProponenteNaoInformadoException;
import com.example.financiamento.domain.exception.VeiculoMuitoAntigoException;
import com.example.financiamento.domain.specification.CadeiaValidacao;
import com.example.financiamento.domain.specification.ValidadorRegras;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;

import static com.example.financiamento.domain.specification.RegraNegocio.de;
import static com.example.financiamento.domain.valueobject.TipoDocumento.COMPROVANTE_RENDA;
import static com.example.financiamento.domain.valueobject.TipoDocumento.CRLV;
import static com.example.financiamento.domain.valueobject.TipoDocumento.IDENTIDADE;
import static com.example.financiamento.domain.valueobject.TipoDocumento.MATRICULA_IMOVEL;

/**
 * CATÁLOGO das regras do financiamento — a "tabela de regras", lida de uma vez — agrupadas pelo MOMENTO
 * em que valem (as regras e os números são de EXEMPLO, simplificados):
 * <ul>
 *   <li>{@link #DOCUMENTOS}: ao salvar a seção de documentos (o financiamento pode continuar incompleto);</li>
 *   <li>{@link #SUBMISSAO}: completude + crédito, numa {@link CadeiaValidacao} (Chain of Responsibility):
 *       o primeiro elo vale sempre; os outros, só para o seu tipo de financiamento;</li>
 *   <li>{@link #CONFORMIDADE_REGISTRO}: CLASSIFICA o retorno do registro da garantia (fato), não recusa.</li>
 * </ul>
 * Regra simples = uma linha {@code de(condição, exceção)} aqui. Vira classe própria só se for parametrizada
 * ({@link PrazoSpec}, {@link PercentualFinanciadoSpec}, {@link DocumentosExigidosSpec}).
 * A ordem é a ordem de avaliação (fail-fast).
 */
public final class RegrasFinanciamento {

    /** A parcela não pode passar deste percentual da renda mensal. */
    public static final int PERCENTUAL_MAXIMO_DA_RENDA = 30;
    public static final int IDADE_MAXIMA_DO_VEICULO = 10;

    // ======================= seção (a cada salvamento, em EM_EDICAO) =======================

    public static final ValidadorRegras<DocumentosDoFinanciamento> DOCUMENTOS = ValidadorRegras.de(
            de(d -> !d.possuiRepetidos(), d -> new DocumentoRepetidoException()));

    // ======================= submissão: Chain of Responsibility por tipo =======================

    private static final ValidadorRegras<SubmissaoFinanciamento> COMUNS = ValidadorRegras.<SubmissaoFinanciamento>de(
            de(s -> s.proponente() != null, s -> new ProponenteNaoInformadoException()),
            de(s -> s.valorSolicitado() != null && s.valorSolicitado().ehPositivo() && s.prazoMeses() != null
                    && s.prazoMeses() > 0, s -> new CondicoesNaoInformadasException()),
            de(s -> s.bem() != null, s -> new GarantiaNaoInformadaException()),
            new DocumentosExigidosSpec(IDENTIDADE, COMPROVANTE_RENDA),
            de(s -> !s.parcela().maiorQue(s.proponente().rendaMensal().percentual(PERCENTUAL_MAXIMO_DA_RENDA)),
                    s -> new ParcelaAcimaDaRendaException(s.parcela(), PERCENTUAL_MAXIMO_DA_RENDA)));

    public static final CadeiaValidacao<SubmissaoFinanciamento> SUBMISSAO = CadeiaValidacao
            .comecandoPor(COMUNS)
            .depois(s -> s.tipo() == TipoFinanciamento.IMOVEL, ValidadorRegras.<SubmissaoFinanciamento>de(
                    new PrazoSpec(60, 420),
                    new PercentualFinanciadoSpec(80),
                    new DocumentosExigidosSpec(MATRICULA_IMOVEL)))
            .depois(s -> s.tipo() == TipoFinanciamento.VEICULO, ValidadorRegras.<SubmissaoFinanciamento>de(
                    new PrazoSpec(12, 60),
                    new PercentualFinanciadoSpec(90),
                    new DocumentosExigidosSpec(CRLV),
                    de(s -> s.hoje().getYear() - s.bem().anoModelo() <= IDADE_MAXIMA_DO_VEICULO,
                            s -> new VeiculoMuitoAntigoException(s.bem().anoModelo(), IDADE_MAXIMA_DO_VEICULO))));

    // ======================= conformidade do registro da garantia (classifica FATO) =======================

    public static final ValidadorRegras<ConferenciaRegistro> CONFORMIDADE_REGISTRO = ValidadorRegras.de(
            de(c -> !c.retorno().registrado() || c.retorno().identificacaoRegistrada().equals(c.bem().identificacao()),
                    c -> new IdentificacaoRegistradaDivergenteException(c.retorno().identificacaoRegistrada(),
                            c.bem().identificacao())));

    private RegrasFinanciamento() {
    }
}
