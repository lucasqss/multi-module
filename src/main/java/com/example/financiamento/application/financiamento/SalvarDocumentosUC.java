package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.application.financiamento.dto.SalvarDocumentosInput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Salva a SEÇÃO "documentos" inteira (PUT). Um caso de uso por seção, não um por documento:
 * a intenção é "definir os documentos deste financiamento".
 */
@ApplicationScoped
public class SalvarDocumentosUC {

    private final FinanciamentoRepository repository;

    public SalvarDocumentosUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinanciamentoOutput executar(SalvarDocumentosInput input) {
        var financiamento = repository.obterParaAlteracao(input.id());
        financiamento.salvarDocumentos(input.documentos());
        repository.salvar(financiamento);
        return FinanciamentoOutput.de(financiamento);
    }
}
