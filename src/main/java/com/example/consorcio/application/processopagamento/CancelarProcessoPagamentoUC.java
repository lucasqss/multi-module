package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.Motivo;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Transição ABERTO|REJEITADO -> CANCELADO. Os demais estados recusam (409). */
@ApplicationScoped
public class CancelarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public CancelarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id, String motivo) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.cancelar(new Motivo(motivo));
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
