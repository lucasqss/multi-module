package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.ListarProcessosPagamentoInput;
import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.shared.utils.Pagina;
import jakarta.enterprise.context.ApplicationScoped;

/** READ (lista paginada com filtros). */
@ApplicationScoped
public class ListarProcessosPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ListarProcessosPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    public Pagina<ProcessoPagamentoOutput> executar(ListarProcessosPagamentoInput input) {
        return repository.listar(input.filtro(), input.pagina(), input.tamanho())
                .map(ProcessoPagamentoOutput::de);
    }
}
