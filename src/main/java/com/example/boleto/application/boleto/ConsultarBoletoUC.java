package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/** READ (um). */
@ApplicationScoped
public class ConsultarBoletoUC {

    private final BoletoRepository repository;

    public ConsultarBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    public BoletoOutput executar(UUID id) {
        return BoletoOutput.de(repository.obter(new BoletoId(id)));
    }
}
