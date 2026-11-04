package com.example.pagamento.application.pagamento;

import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.application.pagamento.dto.RetornoBancoInput;
import com.example.pagamento.domain.repository.PagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * O BANCO informa o resultado (chamado pelo webhook, não pelo usuário). É um FATO: o domínio não o
 * recusa por regra, só o classifica (PAGO, RECUSADO ou EM_ANALISE). Retornos repetidos ou de envios
 * antigos são ignorados pela entidade e respondem 200 (idempotência); o lock impede que dois avisos
 * simultâneos sejam aplicados juntos.
 */
@ApplicationScoped
public class ReceberRetornoBancoUC {

    private final PagamentoRepository repository;

    public ReceberRetornoBancoUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PagamentoOutput executar(RetornoBancoInput input) {
        var retorno = input.retorno();
        var pagamento = repository.obterParaAlteracao(retorno.idTransacao().pagamento()); // o id é nosso: leva ao pagamento
        pagamento.registrarRetornoBanco(retorno);
        repository.salvar(pagamento);
        return PagamentoOutput.de(pagamento);
    }
}
