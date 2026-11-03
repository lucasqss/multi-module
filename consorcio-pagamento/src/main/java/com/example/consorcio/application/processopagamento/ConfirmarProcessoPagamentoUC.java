package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Transição EM_PROCESSAMENTO -> CONFIRMADO (ex.: retorno positivo da liquidação). */
@ApplicationScoped
public class ConfirmarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ConfirmarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.confirmar();
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
