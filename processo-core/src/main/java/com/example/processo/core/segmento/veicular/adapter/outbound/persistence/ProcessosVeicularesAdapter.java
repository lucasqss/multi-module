package com.example.processo.core.segmento.veicular.adapter.outbound.persistence;

import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.veicular.adapter.outbound.persistence.mapper.ProcessoVeicularMapper;
import com.example.processo.core.segmento.veicular.adapter.outbound.persistence.repository.ProcessoVeicularRepository;
import com.example.processo.core.segmento.veicular.application.port.out.ProcessosVeiculares;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** Implementa o port do segmento com o repository (Jakarta Data) e o mapper (domínio ↔ entidade). */
@ApplicationScoped
public class ProcessosVeicularesAdapter implements ProcessosVeiculares {

    private final ProcessoVeicularRepository repositorio;

    ProcessosVeicularesAdapter(ProcessoVeicularRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void salvar(Processo<DadosVeiculares> processo) {
        repositorio.inserir(ProcessoVeicularMapper.paraEntidade(processo));
    }

    @Override
    public Optional<Processo<DadosVeiculares>> porId(UUID id) {
        return repositorio.buscar(id).map(ProcessoVeicularMapper::paraDominio);
    }
}
