package br.com.consorcio.utilizacao.dominio.modalidade.imovel.aplicacao;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;

import java.util.List;

import static br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso.*;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase.*;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro.manual;
import static br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro.portao;

/** Etapa 1 do roadmap: F0 a F16 (docs/roadmap/etapa-01-imovel-urbano). A jornada de negócio mora aqui. */
public class RoteiroImovelUrbanoPronto implements Roteiro {

    private static final List<FaseRoteiro> FASES = List.of(
            manual(CREDITO_DISPONIVEL).peloSistema(),                                          // F0
            portao(ABERTURA, ABERTO, CREDITO_DISPONIVEL).peloSistema(),                        // F1
            portao(DOCUMENTOS, EM_ANALISE, ABERTURA),                                          // F2
            manual(QUALIFICACAO_BENEFICIARIO, DOCUMENTOS),                                     // F3
            manual(CAPACIDADE, DOCUMENTOS),                                                    // F4
            manual(ANALISE_JURIDICA, DOCUMENTOS, QUALIFICACAO_BENEFICIARIO),                   // F5
            manual(VISTORIA_AVALIACAO, ABERTURA),                                              // F6
            portao(DECISAO_COMPLEMENTO, APROVADO,
                    QUALIFICACAO_BENEFICIARIO, CAPACIDADE, ANALISE_JURIDICA, VISTORIA_AVALIACAO), // F7
            manual(SEGUROS, DECISAO_COMPLEMENTO),                                              // F8
            portao(REDACAO_INSTRUMENTO, EM_FORMALIZACAO, DECISAO_COMPLEMENTO),                 // F9
            portao(ASSINATURAS, EM_CONSTITUICAO_GARANTIA, REDACAO_INSTRUMENTO),                // F10
            manual(TRIBUTO_TRANSMISSAO, ASSINATURAS),                                          // F11 ITBI
            manual(CONSTITUICAO_GARANTIA, TRIBUTO_TRANSMISSAO),                                // F12
            portao(CONFERENCIA_GARANTIA, APTO_PAGAMENTO, CONSTITUICAO_GARANTIA, SEGUROS),      // F13
            portao(LIBERACAO_PAGAMENTO, PAGO, CONFERENCIA_GARANTIA).peloSistema(),             // F14
            manual(ENTREGA, LIBERACAO_PAGAMENTO),                                              // F15
            portao(SOBRA_ENCERRAMENTO, CONCLUIDO, LIBERACAO_PAGAMENTO, ENTREGA)                // F16
    );

    @Override
    public String codigo() { return "IMOVEL_URBANO_PRONTO"; }

    @Override
    public int versao() { return 1; }

    @Override
    public Finalidade finalidade() { return Finalidade.AQUISICAO; }

    @Override
    public List<FaseRoteiro> fases() { return FASES; }
}
