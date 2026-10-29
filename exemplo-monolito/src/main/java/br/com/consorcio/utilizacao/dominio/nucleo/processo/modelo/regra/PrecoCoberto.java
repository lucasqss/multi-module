package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.regra;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Objeto;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.PreCondicaoFase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

/** Aprovar exige o preço coberto por cotas reservadas + recursos comprovados, e a carta limitada ao valor base (CA-E1-06). */
@ApplicationScoped
public class PrecoCoberto implements PreCondicaoFase {

    @Inject
    CreditoApi credito;

    @Override
    public boolean aplicaA(FaseRoteiro fase) {
        return fase.levaA(EstadoProcesso.APROVADO);
    }

    @Override
    public void verificar(Processo processo) {
        BigDecimal preco = processo.objetos().stream().map(Objeto::valorNegociado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal limiteCarta = processo.objetos().stream().map(Objeto::valorBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal carta = credito.totalReservado(processo.id());
        BigDecimal recursos = credito.recursosComprovados(processo.id());
        RegraNegocioException.exigir(carta.compareTo(limiteCarta) <= 0, "CARTA_ACIMA_DO_AVALIADO",
                "Reservado " + carta + " excede o menor entre negociado e avaliado (" + limiteCarta + ")");
        RegraNegocioException.exigir(carta.add(recursos).compareTo(preco) >= 0, "MOT-COMPLEMENTO",
                "Preço " + preco + " não coberto: carta " + carta + " + recursos comprovados " + recursos);
    }
}
