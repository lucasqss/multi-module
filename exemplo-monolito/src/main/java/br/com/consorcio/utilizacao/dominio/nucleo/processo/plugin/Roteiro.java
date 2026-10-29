package br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;

import java.util.List;
import java.util.Optional;

/** Jornada de negócio de uma modalidade + finalidade. Mudar = nova versão; processos abertos seguem na sua. */
public interface Roteiro {

    String codigo();

    int versao();

    Finalidade finalidade();

    /** Em ordem de exibição. */
    List<FaseRoteiro> fases();

    default Optional<FaseRoteiro> fase(Fase fase) {
        return fases().stream().filter(f -> f.fase() == fase).findFirst();
    }
}
