package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.application.pagamento.dto.RetornoBancoInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * O BANCO informa o resultado (chamado pelo webhook, não pelo usuário). É um FATO: o domínio não o
 * recusa por regra, só o classifica (PAGO, RECUSADO ou EM_ANALISE). Retornos repetidos são ignorados
 * pela entidade (idempotência) e o lock impede que dois avisos simultâneos sejam aplicados juntos.
 */
@ApplicationScoped
public class ReceberRetornoBancoUC {

    private final PagamentoRepository repository;

    public ReceberRetornoBancoUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PagamentoOutput executar(RetornoBancoInput input) {
        var pagamento = repository.obterPorTransacaoParaAlteracao(input.idTransacao());
        pagamento.registrarRetornoBanco(input.retorno());
        repository.salvar(pagamento);
        return PagamentoOutput.de(pagamento);
    }
}
