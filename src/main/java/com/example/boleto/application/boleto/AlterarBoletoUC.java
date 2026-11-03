package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.AlterarBoletoInput;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** UPDATE da seção "dados gerais" (vencimento). Só em EM_EDICAO: quem decide é o estado. */
@ApplicationScoped
public class AlterarBoletoUC {

    private final BoletoRepository repository;

    public AlterarBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(AlterarBoletoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        boleto.alterarVencimento(input.dataVencimento());
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
