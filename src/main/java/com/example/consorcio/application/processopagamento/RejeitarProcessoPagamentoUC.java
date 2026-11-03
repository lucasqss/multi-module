package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.Motivo;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Transição EM_PROCESSAMENTO -> REJEITADO (ex.: retorno negativo da liquidação). */
@ApplicationScoped
public class RejeitarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public RejeitarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id, String motivo) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.rejeitar(new Motivo(motivo));
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
