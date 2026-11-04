package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.AlterarDadosInput;
import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE da seção "dados gerais" (proponente, valor solicitado e prazo). */
@ApplicationScoped
public class AlterarDadosFinanciamentoUC {

    private final FinanciamentoRepository repository;

    public AlterarDadosFinanciamentoUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinanciamentoOutput executar(AlterarDadosInput input) {
        var financiamento = repository.obterParaAlteracao(input.id());
        financiamento.alterarDadosGerais(input.proponente(), input.valor(), input.prazoMeses());
        repository.salvar(financiamento);
        return FinanciamentoOutput.de(financiamento);
    }
}
