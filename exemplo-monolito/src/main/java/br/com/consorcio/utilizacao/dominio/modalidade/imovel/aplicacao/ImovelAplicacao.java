package br.com.consorcio.utilizacao.dominio.modalidade.imovel.aplicacao;

import br.com.consorcio.utilizacao.dominio.modalidade.imovel.api.ImovelApi;
import br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo.ObjetoImovel;
import br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo.ProcessoImobiliario;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.AberturaDeProcesso;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ImovelAplicacao implements ImovelApi {

    @Inject
    AberturaDeProcesso abertura;

    @Override
    @Transactional
    public Long abrir(AbrirProcessoImovel cmd, String ator) {
        ProcessoImobiliario processo = new ProcessoImobiliario(Finalidade.AQUISICAO, cmd.tipoOperacao(), cmd.municipio());
        ObjetoImovel imovel = new ObjetoImovel(cmd.matricula(), cmd.cartorioRi(), cmd.inscricaoIptu(), cmd.uf(),
                cmd.municipio(), cmd.construcaoAverbada(), cmd.valorNegociado());
        if (cmd.contatoNome() != null) {
            imovel.adicionarContatoAcesso(cmd.contatoNome(), cmd.contatoEmail(), cmd.contatoTelefone());
        }
        return abertura.abrir(processo, imovel, "PF", cmd.cpfTitular(), cmd.nomeTitular(), ator);
    }
}
