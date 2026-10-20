package com.example.processo.core.motor.adapter.outbound.persistence;

import com.example.processo.core.motor.adapter.outbound.persistence.repository.ProcessoRepository;
import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.domain.state.Situacao;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProcessosAdapter implements Processos {

    private final ProcessoRepository repositorio;

    ProcessosAdapter(ProcessoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<String> segmentoDe(UUID id) {
        return repositorio.buscarSegmento(id);
    }

    /** Compare-and-set: só uma linha muda se a situação ainda for a anterior. */
    @Override
    public boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
        return repositorio.atualizarSituacao(id, de.name(), para.name(), motivoRecusa) == 1;
    }
}
