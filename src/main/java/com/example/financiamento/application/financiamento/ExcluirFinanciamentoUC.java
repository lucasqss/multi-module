package com.example.financiamento.application.financiamento;

import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** DELETE físico: só rascunho (o status decide). Depois de submetido, o caminho é cancelar. */
@ApplicationScoped
public class ExcluirFinanciamentoUC {

    private final FinanciamentoRepository repository;

    public ExcluirFinanciamentoUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void executar(UUID id) {
        repository.obterParaAlteracao(id).validarExclusao();
        repository.remover(id);
    }
}
