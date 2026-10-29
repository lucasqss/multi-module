package br.com.consorcio.utilizacao.dominio.modalidade.imovel.aplicacao;

import br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo.regra.ValorAvaliadoInformado;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Modalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.PreCondicaoFase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/** Registro do plug-in. Novo roteiro (planta, construção, quitação) = mais um item na lista. */
@ApplicationScoped
public class ModalidadeImovel implements Modalidade {

    private static final List<Roteiro> ROTEIROS = List.of(new RoteiroImovelUrbanoPronto());
    private static final List<PreCondicaoFase> REGRAS = List.of(new ValorAvaliadoInformado());

    @Override
    public TipoProcesso tipo() { return TipoProcesso.IMOBILIARIO; }

    @Override
    public List<Roteiro> roteiros() { return ROTEIROS; }

    @Override
    public List<PreCondicaoFase> regras() { return REGRAS; }
}
