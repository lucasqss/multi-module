package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Transição REJEITADO -> ABERTO, para corrigir e reenviar. */
@ApplicationScoped
public class ReabrirProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ReabrirProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.reabrir();
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
