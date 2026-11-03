package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Transição ABERTO -> EM_PROCESSAMENTO (ex.: disparado pelo envio à liquidação). */
@ApplicationScoped
public class EnviarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public EnviarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.enviarParaProcessamento();
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
