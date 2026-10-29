package br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo.regra;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.PreCondicaoFase;

/**
 * Regra só do imóvel: a vistoria não termina sem laudo com valor. Não é bean CDI de propósito:
 * chega ao núcleo por ModalidadeImovel.regras(), então não vale para as outras modalidades.
 */
public class ValorAvaliadoInformado implements PreCondicaoFase {

    @Override
    public boolean aplicaA(FaseRoteiro fase) {
        return fase.fase() == Fase.VISTORIA_AVALIACAO;
    }

    @Override
    public void verificar(Processo processo) {
        RegraNegocioException.exigir(processo.objetos().stream().allMatch(o -> o.valorAvaliado() != null),
                "LAUDO_PENDENTE", "Informe o valor avaliado de todos os imóveis");
    }
}
