package br.com.consorcio.utilizacao.dominio.modalidade.servico.aplicacao;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Modalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ModalidadeServico implements Modalidade {

    private static final List<Roteiro> ROTEIROS = List.of(new RoteiroServico());

    @Override
    public TipoProcesso tipo() { return TipoProcesso.SERVICO; }

    @Override
    public List<Roteiro> roteiros() { return ROTEIROS; }
}
