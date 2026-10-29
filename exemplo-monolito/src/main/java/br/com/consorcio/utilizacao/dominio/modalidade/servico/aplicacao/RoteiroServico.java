package br.com.consorcio.utilizacao.dominio.modalidade.servico.aplicacao;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;

import java.util.List;

import static br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso.*;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase.*;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro.manual;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro.portao;

/**
 * Exemplo de encaixe: serviço sem alienação do objeto. A fiança costuma vir da contemplação; o portão
 * para APTO_PAGAMENTO é a assinatura, e a cobertura continua verificada porque a regra olha o portão.
 */
public class RoteiroServico implements Roteiro {

    private static final List<FaseRoteiro> FASES = List.of(
            manual(CREDITO_DISPONIVEL).peloSistema(),
            portao(ABERTURA, ABERTO, CREDITO_DISPONIVEL).peloSistema(),
            portao(DOCUMENTOS, EM_ANALISE, ABERTURA),
            manual(QUALIFICACAO_BENEFICIARIO, DOCUMENTOS),
            manual(CAPACIDADE, DOCUMENTOS),
            portao(DECISAO_COMPLEMENTO, APROVADO, QUALIFICACAO_BENEFICIARIO, CAPACIDADE),
            portao(REDACAO_INSTRUMENTO, EM_FORMALIZACAO, DECISAO_COMPLEMENTO),
            portao(ASSINATURAS, APTO_PAGAMENTO, REDACAO_INSTRUMENTO),
            portao(LIBERACAO_PAGAMENTO, PAGO, ASSINATURAS).peloSistema(),
            portao(SOBRA_ENCERRAMENTO, CONCLUIDO, LIBERACAO_PAGAMENTO)
    );

    @Override
    public String codigo() { return "SERVICO"; }

    @Override
    public int versao() { return 1; }

    @Override
    public Finalidade finalidade() { return Finalidade.AQUISICAO; }

    @Override
    public List<FaseRoteiro> fases() { return FASES; }
}
