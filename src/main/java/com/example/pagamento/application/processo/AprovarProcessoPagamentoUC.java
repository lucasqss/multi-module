package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.Matricula;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Um aprovador aprova. A ALÇADA (decisão do domínio) diz se já basta ou se o processo continua aguardando
 * outra aprovação.
 */
@ApplicationScoped
public class AprovarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;
    private final Clock clock;

    public AprovarProcessoPagamentoUC(ProcessoPagamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id, String aprovador) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.aprovar(new Matricula(aprovador), clock.instant());
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
