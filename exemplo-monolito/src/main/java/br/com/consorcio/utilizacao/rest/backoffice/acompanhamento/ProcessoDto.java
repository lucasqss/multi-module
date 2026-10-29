package br.com.consorcio.utilizacao.rest.backoffice.acompanhamento;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi.LinhaDoTempo;

import java.math.BigDecimal;
import java.util.List;

/** Tela de acompanhamento do backoffice: tudo do processo numa resposta. */
public record ProcessoDto(Long id, String tipo, String estado, String roteiro, List<Objeto> objetos,
                          List<Etapa> etapas, List<Cota> cotas, List<Garantia> garantias) {

    public record Objeto(Long id, String descricao, BigDecimal valorNegociado, BigDecimal valorAvaliado) {
    }

    public record Etapa(String fase, String situacao) {
    }

    public record Cota(Long id, String cotaRef, String situacao, BigDecimal reservado, BigDecimal disponivel) {
    }

    public record Garantia(Long id, String cotaRef, String tipo, String situacao) {
    }

    static ProcessoDto de(LinhaDoTempo l) {
        var p = l.processo();
        return new ProcessoDto(p.id(), p.tipo().name(), p.estado().name(), p.roteiro(),
                p.objetos().stream().map(o -> new Objeto(o.id(), o.descricao(), o.valorNegociado(), o.valorAvaliado())).toList(),
                p.etapas().stream().map(e -> new Etapa(e.fase().name(), e.situacao().name())).toList(),
                l.cotas().stream().map(c -> new Cota(c.id(), c.cotaRef(), c.situacao(), c.reservado(), c.disponivel())).toList(),
                l.garantias().stream().map(g -> new Garantia(g.id(), g.cotaRef(), g.tipo(), g.situacao())).toList());
    }
}
