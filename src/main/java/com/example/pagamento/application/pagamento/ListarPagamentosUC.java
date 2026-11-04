package com.example.pagamento.application.pagamento;

import com.example.pagamento.application.pagamento.dto.ListarPagamentosInput;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.repository.PagamentoRepository;
import com.example.pagamento.shared.Pagina;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** READ (lista paginada com filtros). */
@ApplicationScoped
public class ListarPagamentosUC {

    private final PagamentoRepository repository;

    public ListarPagamentosUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Pagina<PagamentoOutput> executar(ListarPagamentosInput input) {
        return repository.listar(input.modalidade(), input.status(), input.pagina(), input.tamanho()).map(PagamentoOutput::de);
    }
}
