package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.valueobject.Modalidade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * CREATE: cria o rascunho (EM_EDICAO) só com a modalidade. As seções são salvas por casos de uso
 * próprios; a completude é exigida na submissão ({@code SubmeterPagamento}).
 */
@ApplicationScoped
public class CriarPagamentoUC {

    private final PagamentoRepository repository;
    private final Clock clock;

    public CriarPagamentoUC(PagamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public PagamentoOutput executar(Modalidade modalidade) {
        var pagamento = Pagamento.criar(modalidade, clock.instant());
        repository.salvar(pagamento);
        return PagamentoOutput.de(pagamento);
    }
}
