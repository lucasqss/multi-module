package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** READ (um). Leitura sem lock; a transação só mantém a sessão aberta para carregar os documentos. */
@ApplicationScoped
public class ConsultarFinanciamentoUC {

    private final FinanciamentoRepository repository;

    public ConsultarFinanciamentoUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinanciamentoOutput executar(UUID id) {
        return FinanciamentoOutput.de(repository.obter(id));
    }
}
