package br.com.consorcio.utilizacao.rest.backoffice.formalizacao;

import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.api.FormalizacaoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.EnumSet;
import java.util.Set;

/**
 * Compõe a tela de formalização a partir das APIs de processo, formalização e garantia.
 * Reorganizar a tela muda só esta classe e o DTO; nenhuma regra mora aqui.
 */
@ApplicationScoped
public class FormalizacaoMontador {

    private static final Set<Fase> FASES_DA_TELA = EnumSet.of(Fase.SEGUROS, Fase.REDACAO_INSTRUMENTO, Fase.ASSINATURAS,
            Fase.TRIBUTO_TRANSMISSAO, Fase.CONSTITUICAO_GARANTIA, Fase.CONFERENCIA_GARANTIA);

    @Inject
    ProcessoApi processos;

    @Inject
    FormalizacaoApi formalizacao;

    @Inject
    GarantiaApi garantias;

    public PainelFormalizacao painel(Long processoId) {
        var p = processos.resumo(processoId);
        var instrumento = formalizacao.instrumentoDo(processoId)
                .map(i -> new PainelFormalizacao.Instrumento(i.id(), i.tipo(), i.situacao())).orElse(null);
        return new PainelFormalizacao(p.id(), p.estado().name(), instrumento,
                garantias.doProcesso(processoId).stream()
                        .map(g -> new PainelFormalizacao.Garantia(g.id(), g.cotaRef(), g.tipo(), g.situacao())).toList(),
                p.etapas().stream().filter(e -> FASES_DA_TELA.contains(e.fase()) && !e.situacao().resolvida())
                        .map(e -> e.fase().name()).toList());
    }
}
