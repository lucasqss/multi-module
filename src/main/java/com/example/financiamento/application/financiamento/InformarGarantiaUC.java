package com.example.financiamento.application.financiamento;

import com.example.financiamento.adapter.avaliacao.AvaliadorDeBens;
import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * Salva a SEÇÃO "garantia". O usuário informa só a identificação do bem (matrícula ou placa); os DADOS
 * (descrição, valor avaliado, ano) vêm do SISTEMA DE AVALIAÇÃO, que é quem avalia o bem.
 */
@ApplicationScoped
public class InformarGarantiaUC {

    private final FinanciamentoRepository repository;
    private final AvaliadorDeBens avaliador;

    public InformarGarantiaUC(FinanciamentoRepository repository, AvaliadorDeBens avaliador) {
        this.repository = repository;
        this.avaliador = avaliador;
    }

    @Transactional
    public FinanciamentoOutput executar(UUID id, String identificacao) {
        var financiamento = repository.obterParaAlteracao(id);
        var bem = avaliador.avaliar(financiamento.getTipo(), identificacao);

        financiamento.informarGarantia(bem);

        repository.salvar(financiamento);
        return FinanciamentoOutput.de(financiamento);
    }
}
