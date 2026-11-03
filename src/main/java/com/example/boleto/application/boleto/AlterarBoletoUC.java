package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.AlterarBoletoInput;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.specification.boleto.SolicitacaoBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;

/** UPDATE. O estado decide SE pode alterar; as specifications decidem se os dados novos são válidos. */
@ApplicationScoped
public class AlterarBoletoUC {

    private final BoletoRepository repository;
    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public AlterarBoletoUC(BoletoRepository repository, Clock clock,
                           @ConfigProperty(name = "boleto.prazo-maximo-vencimento-dias") int prazoMaximoVencimentoDias) {
        this.repository = repository;
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    @Transactional
    public BoletoOutput executar(AlterarBoletoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        var novosDados = boleto.getDados().comValorEVencimento(input.valor(), input.dataVencimento());

        boleto.alterar(SolicitacaoBoleto.paraAlteracao(novosDados, clock, prazoMaximoVencimentoDias));

        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
