package com.example.processo.core.motor.adapter.outbound.persistence;

import com.example.processo.core.motor.adapter.outbound.persistence.repository.NumeracaoRepository;
import com.example.processo.core.motor.application.port.out.Numeracao;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NumeracaoAdapter implements Numeracao {

    private final NumeracaoRepository repositorio;

    NumeracaoAdapter(NumeracaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public long proximo() {
        return repositorio.proximo();
    }
}
