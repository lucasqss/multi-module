package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.AlterarProcessoInput;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE da seção "dados gerais" (conta para débito e data de pagamento). Só em EM_EDICAO. */
@ApplicationScoped
public class AlterarProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public AlterarProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(AlterarProcessoInput input) {
        var processo = repository.obter(new ProcessoPagamentoId(input.id()));
        processo.alterarDadosGerais(input.conta(), input.dataPagamento());
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
