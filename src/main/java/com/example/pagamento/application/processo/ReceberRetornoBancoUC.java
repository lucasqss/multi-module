package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.application.processo.dto.RetornoBancoInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * O BANCO informa o resultado de uma tentativa (chamado pelo webhook, não pelo usuário).
 * É um FATO: o domínio não o recusa por regra, só o classifica (PAGO, FALHOU ou EM_ANALISE).
 * Retornos repetidos da mesma transação são ignorados pela entidade (idempotência).
 */
@ApplicationScoped
public class ReceberRetornoBancoUC {

    private final ProcessoPagamentoRepository repository;

    public ReceberRetornoBancoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(RetornoBancoInput input) {
        var processo = repository.obterPorTransacao(input.idTransacao());
        processo.registrarRetornoBanco(input.retorno());
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
