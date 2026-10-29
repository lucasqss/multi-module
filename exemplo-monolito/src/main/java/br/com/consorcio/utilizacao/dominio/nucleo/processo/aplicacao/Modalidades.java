package br.com.consorcio.utilizacao.dominio.nucleo.processo.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Modalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Comparator;

/** Registro dos plug-ins: o núcleo descobre as modalidades por CDI, sem conhecê-las pelo nome. */
@ApplicationScoped
public class Modalidades {

    @Inject
    Instance<Modalidade> todas;

    public Modalidade de(TipoProcesso tipo) {
        return todas.stream().filter(m -> m.tipo() == tipo).findFirst()
                .orElseThrow(() -> new RegraNegocioException("MODALIDADE_INEXISTENTE", "Não há plug-in para " + tipo));
    }

    public Roteiro vigente(TipoProcesso tipo, Finalidade finalidade) {
        return de(tipo).roteiros().stream()
                .filter(r -> r.finalidade() == finalidade)
                .max(Comparator.comparingInt(Roteiro::versao))
                .orElseThrow(() -> new RegraNegocioException("ROTEIRO_INEXISTENTE", "Não há roteiro para " + tipo + " / " + finalidade));
    }

    /** Processo aberto segue o roteiro (e a versão) com que nasceu. */
    public Roteiro roteiroDe(Processo processo) {
        return de(processo.tipo()).roteiros().stream()
                .filter(r -> r.codigo().equals(processo.roteiroCodigo()) && r.versao() == processo.roteiroVersao())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Roteiro " + processo.roteiroCodigo() + " v" + processo.roteiroVersao() + " não está mais no código"));
    }
}
