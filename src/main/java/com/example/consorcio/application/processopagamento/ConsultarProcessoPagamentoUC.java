package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/** READ (um). */
@ApplicationScoped
public class ConsultarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ConsultarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    public ProcessoPagamentoOutput executar(UUID id) {
        return ProcessoPagamentoOutput.de(repository.obter(new ProcessoPagamentoId(id)));
    }
}
