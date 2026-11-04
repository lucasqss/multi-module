package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.application.pagamento.dto.AlterarDadosInput;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.valueobject.PagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE da seção "dados gerais" (conta de débito e data de pagamento). */
@ApplicationScoped
public class AlterarDadosPagamentoUC {

    private final PagamentoRepository repository;

    public AlterarDadosPagamentoUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PagamentoOutput executar(AlterarDadosInput input) {
        var pagamento = repository.obterParaAlteracao(new PagamentoId(input.id()));
        pagamento.alterarDadosGerais(input.conta(), input.dataPagamento());
        repository.salvar(pagamento);
        return PagamentoOutput.de(pagamento);
    }
}
