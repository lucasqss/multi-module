package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.regra;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.PreCondicaoFase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * "Só paga com garantia constituída": vale para o portão que leva a APTO_PAGAMENTO em qualquer roteiro
 * (conferência do registro no imóvel; assinatura no serviço com fiança da contemplação).
 */
@ApplicationScoped
public class GarantiaSuficiente implements PreCondicaoFase {

    @Inject
    CreditoApi credito;

    @Inject
    GarantiaApi garantias;

    @Override
    public boolean aplicaA(FaseRoteiro fase) {
        return fase.levaA(EstadoProcesso.APTO_PAGAMENTO);
    }

    @Override
    public void verificar(Processo processo) {
        for (CreditoApi.CotaReservada cota : credito.reservadas(processo.id())) {
            GarantiaApi.Cobertura c = garantias.cobertura(cota.cotaRef(), cota.reservado());
            RegraNegocioException.exigir(c.suficiente(), "GARANTIA_INSUFICIENTE",
                    "Cota " + cota.cotaRef() + ": exigido " + c.exigido() + ", constituído " + c.constituido());
        }
    }
}
