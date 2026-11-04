package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** O financeiro analisa o pagamento divergente e o aceita como quitação. EM_ANALISE -> PAGO. */
@ApplicationScoped
public class AceitarPagamentoBoletoUC {

    private final BoletoRepository repository;

    public AceitarPagamentoBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(UUID id) {
        var boleto = repository.obter(new BoletoId(id));
        boleto.aceitarPagamento();
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
