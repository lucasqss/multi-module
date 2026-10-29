package br.com.consorcio.utilizacao.rest.backoffice.formalizacao;

import java.util.List;

/** Uma tela que junta três módulos: o motivo de existir um montador. */
public record PainelFormalizacao(Long processoId, String estado, Instrumento instrumento, List<Garantia> garantias,
                                 List<String> fasesPendentes) {

    public record Instrumento(Long id, String tipo, String situacao) {
    }

    public record Garantia(Long id, String cotaRef, String tipo, String situacao) {
    }
}
