package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.application.financiamento.dto.ListarFinanciamentosInput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import com.example.financiamento.shared.Pagina;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** READ (lista paginada com filtros: quem trata os filtros é o repositório). */
@ApplicationScoped
public class ListarFinanciamentosUC {

    private final FinanciamentoRepository repository;

    public ListarFinanciamentosUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Pagina<FinanciamentoOutput> executar(ListarFinanciamentosInput input) {
        return repository.listar(input.tipo(), input.status(), input.pagina(), input.tamanho()).map(FinanciamentoOutput::de);
    }
}
