package com.example.financiamento.application.financiamento;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.application.financiamento.dto.RetornoRegistroInput;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * O ÓRGÃO DE REGISTRO informa o resultado (chamado pelo webhook, não pelo usuário). É um FATO: o domínio
 * não o recusa por regra, só o classifica (CONTRATADO, REGISTRO_RECUSADO ou EM_PENDENCIA). Retornos repetidos
 * ou de envios antigos são ignorados pela entidade e respondem 200 (idempotência); o lock impede que dois
 * avisos simultâneos sejam aplicados juntos.
 */
@ApplicationScoped
public class ReceberRetornoRegistroUC {

    private final FinanciamentoRepository repository;

    public ReceberRetornoRegistroUC(FinanciamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinanciamentoOutput executar(RetornoRegistroInput input) {
        var retorno = input.retorno();
        var financiamento = repository.obterParaAlteracao(retorno.idProtocolo().financiamento()); // o protocolo é nosso
        financiamento.registrarRetornoRegistro(retorno);
        repository.salvar(financiamento);
        return FinanciamentoOutput.de(financiamento);
    }
}
