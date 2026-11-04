package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * CREATE: cria o rascunho (EM_EDICAO) só com o tipo. As seções são salvas por casos de uso próprios;
 * a completude é exigida na submissão ({@code SubmeterFinanciamento}).
 */
@ApplicationScoped
public class CriarFinanciamentoUC {

    private final FinanciamentoRepository repository;
    private final Clock clock;

    public CriarFinanciamentoUC(FinanciamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public FinanciamentoOutput executar(TipoFinanciamento tipo) {
        var financiamento = Financiamento.criar(tipo, clock.instant());
        repository.salvar(financiamento);
        return FinanciamentoOutput.de(financiamento);
    }
}
