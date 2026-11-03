package com.example.consorcio.application.pagamento;

import com.example.consorcio.application.pagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsultarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ConsultarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    public ProcessoPagamentoOutput executar(String id) {
        ProcessoPagamentoId processoId;
        try {
            processoId = ProcessoPagamentoId.de(id);
        } catch (IllegalArgumentException e) {
            throw new RecursoNaoEncontradoException("Processo de pagamento", id);
        }
        return repository.buscarPorId(processoId)
                .map(ProcessoPagamentoOutput::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo de pagamento", id));
    }
}
