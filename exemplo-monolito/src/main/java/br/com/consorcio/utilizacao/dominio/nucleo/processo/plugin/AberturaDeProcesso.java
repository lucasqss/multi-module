package br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Objeto;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;

/** Usado pelos plug-ins para entregar ao núcleo o processo e o objeto já especializados. */
public interface AberturaDeProcesso {

    Long abrir(Processo novo, Objeto objeto, String tipoPessoa, String cpfTitular, String nomeTitular, String ator);
}
