package br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;

import java.util.Optional;
import java.util.Set;

/**
 * Uma fase dentro de um roteiro.
 *
 * @param dependeDe        fases que precisam estar concluídas (ou dispensadas) antes
 * @param estadoAoConcluir se presente, a fase é um portão: concluí-la move o estado macro
 * @param automatica       concluída pelo próprio sistema, nunca por um usuário
 */
public record FaseRoteiro(Fase fase, Set<Fase> dependeDe, Optional<EstadoProcesso> estadoAoConcluir, boolean automatica) {

    public static FaseRoteiro manual(Fase fase, Fase... dependeDe) {
        return new FaseRoteiro(fase, Set.of(dependeDe), Optional.empty(), false);
    }

    public static FaseRoteiro portao(Fase fase, EstadoProcesso estado, Fase... dependeDe) {
        return new FaseRoteiro(fase, Set.of(dependeDe), Optional.of(estado), false);
    }

    public FaseRoteiro peloSistema() {
        return new FaseRoteiro(fase, dependeDe, estadoAoConcluir, true);
    }

    public boolean levaA(EstadoProcesso estado) {
        return estadoAoConcluir.filter(e -> e == estado).isPresent();
    }
}
