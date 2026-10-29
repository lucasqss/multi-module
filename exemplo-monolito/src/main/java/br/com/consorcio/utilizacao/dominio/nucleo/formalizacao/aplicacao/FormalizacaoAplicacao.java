package br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.aplicacao;

import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.api.FormalizacaoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.modelo.Instrumento;
import br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.modelo.InstrumentoRepository;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class FormalizacaoAplicacao implements FormalizacaoApi {

    @Inject
    InstrumentoRepository instrumentos;

    @Inject
    ProcessoApi processos;

    @Inject
    CreditoApi credito;

    @Inject
    GarantiaApi garantias;

    /** A garantia é criada pelo módulo garantia; aqui só passamos as referências. */
    @Override
    @Transactional
    public InstrumentoResumo redigirCompraVendaComAlienacao(Long processoId, String ator) {
        ProcessoApi.ProcessoResumo p = processos.resumo(processoId);
        Instrumento instrumento = new Instrumento(processoId, "COMPRA_VENDA_COM_AF");
        instrumentos.persist(instrumento);
        ProcessoApi.ObjetoResumo imovel = p.objetos().get(0);
        for (CreditoApi.CotaReservada cota : credito.reservadas(processoId)) {
            garantias.proporAlienacao(new GarantiaApi.PropostaAlienacao(cota.cotaRef(), "AF_IMOVEL", processoId, imovel.id(),
                    instrumento.id(), imovel.valorBase()), ator);
        }
        processos.concluirEtapa(processoId, Fase.REDACAO_INSTRUMENTO, ator);
        return resumo(instrumento);
    }

    @Override
    public Optional<InstrumentoResumo> instrumentoDo(Long processoId) {
        return instrumentos.find("processoId = ?1 order by id desc", processoId).firstResultOptional().map(FormalizacaoAplicacao::resumo);
    }

    private static InstrumentoResumo resumo(Instrumento i) {
        return new InstrumentoResumo(i.id(), i.processoId(), i.tipoInstrumento(), i.situacao());
    }
}
