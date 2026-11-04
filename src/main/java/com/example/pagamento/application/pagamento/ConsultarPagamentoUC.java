package com.example.pagamento.application.pagamento;

import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.repository.PagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** READ (um). Leitura sem lock; a transação só mantém a sessão aberta para carregar os boletos. */
@ApplicationScoped
public class ConsultarPagamentoUC {

    private final PagamentoRepository repository;

    public ConsultarPagamentoUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PagamentoOutput executar(UUID id) {
        return PagamentoOutput.de(repository.obter(id));
    }
}
