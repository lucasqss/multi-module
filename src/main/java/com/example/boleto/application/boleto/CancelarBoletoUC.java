package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Motivo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** EMITIDO|NOTIFICADO -> CANCELADO. Os estados finais recusam (409). */
@ApplicationScoped
public class CancelarBoletoUC {

    private final BoletoRepository repository;

    public CancelarBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(UUID id, String motivo) {
        var boleto = repository.obter(new BoletoId(id));
        boleto.cancelar(new Motivo(motivo));
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
