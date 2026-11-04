package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/** Finaliza a edição: exige COMPLETUDE (regras de submissão). EM_EDICAO -> AGUARDANDO_APROVACAO. */
@ApplicationScoped
public class SubmeterProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;
    private final Clock clock;

    public SubmeterProcessoPagamentoUC(ProcessoPagamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.submeter(LocalDate.now(clock));
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
