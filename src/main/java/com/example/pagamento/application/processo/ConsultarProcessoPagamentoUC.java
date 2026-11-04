package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
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
