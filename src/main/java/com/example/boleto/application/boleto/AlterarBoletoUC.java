package com.example.boleto.application.boleto;

import com.example.boleto.application.boleto.dto.AlterarBoletoInput;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.factory.SolicitacaoBoletoFactory;
import com.example.boleto.domain.repository.BoletoRepository;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE. O estado decide SE pode alterar; as specifications decidem se os dados novos são válidos. */
@ApplicationScoped
public class AlterarBoletoUC {

    private final BoletoRepository repository;
    private final SolicitacaoBoletoFactory solicitacoes;

    public AlterarBoletoUC(BoletoRepository repository, SolicitacaoBoletoFactory solicitacoes) {
        this.repository = repository;
        this.solicitacoes = solicitacoes;
    }

    @Transactional
    public BoletoOutput executar(AlterarBoletoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        var novosDados = boleto.getDados().comValorEVencimento(input.valor(), input.dataVencimento());

        boleto.alterar(solicitacoes.paraAlteracao(novosDados));

        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
