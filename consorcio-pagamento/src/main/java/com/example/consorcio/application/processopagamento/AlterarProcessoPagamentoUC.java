package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.AlterarProcessoPagamentoInput;
import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.factory.ProcessoPagamentoFactory;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE. O estado decide SE pode alterar; as specifications decidem se os dados novos são válidos. */
@ApplicationScoped
public class AlterarProcessoPagamentoUC {

    private final CotaRepository cotaRepository;
    private final ProcessoPagamentoRepository processoRepository;
    private final ProcessoPagamentoFactory factory;

    public AlterarProcessoPagamentoUC(CotaRepository cotaRepository, ProcessoPagamentoRepository processoRepository,
                                      ProcessoPagamentoFactory factory) {
        this.cotaRepository = cotaRepository;
        this.processoRepository = processoRepository;
        this.factory = factory;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(AlterarProcessoPagamentoInput input) {
        var processo = processoRepository.obter(new ProcessoPagamentoId(input.id()));
        var cota = cotaRepository.obter(processo.getCota());

        processo.alterar(factory.solicitacaoDeAlteracao(cota, input.dadosPagamento()));

        processoRepository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
