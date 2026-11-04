package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ListarProcessosInput;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.shared.Pagina;
import jakarta.enterprise.context.ApplicationScoped;

/** READ (lista paginada com filtros). */
@ApplicationScoped
public class ListarProcessosPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ListarProcessosPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    public Pagina<ProcessoPagamentoOutput> executar(ListarProcessosInput input) {
        return repository.listar(input.filtro(), input.pagina(), input.tamanho()).map(ProcessoPagamentoOutput::de);
    }
}
