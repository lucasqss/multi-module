package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * Cancela o processo. Não é permitido em EM_PROCESSAMENTO (o dinheiro pode já ter saído) nem nos estados
 * finais.
 */
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
