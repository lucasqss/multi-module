package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** Um aprovador rejeita. AGUARDANDO_APROVACAO -> REJEITADO. */
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
