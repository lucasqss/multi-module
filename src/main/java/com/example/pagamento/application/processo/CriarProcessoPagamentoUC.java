package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.valueobject.Matricula;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * CREATE: cria o processo em EM_EDICAO (rascunho) só com o solicitante.
 * As seções são salvas por casos de uso próprios; a completude é exigida em {@link SubmeterProcessoPagamentoUC}.
 */
@ApplicationScoped
public class CriarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;
    private final Clock clock;

    public CriarProcessoPagamentoUC(ProcessoPagamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(String solicitante) {
        var processo = ProcessoPagamento.criar(new Matricula(solicitante), clock.instant());
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
