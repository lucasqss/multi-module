package com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.mapper.ProcessoImobiliarioMapper;
import com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.repository.ProcessoImobiliarioRepository;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implementa o port do segmento com o repository (Jakarta Data) e o mapper (domínio ↔ entidade). */
@ApplicationScoped
public class ProcessosImobiliariosAdapter implements ProcessosImobiliarios {

    private static final List<SituacaoProcesso> EM_ANDAMENTO =
            List.of(SituacaoProcesso.ABERTO, SituacaoProcesso.EM_PROCESSAMENTO);

    private final ProcessoImobiliarioRepository repositorio;

    ProcessosImobiliariosAdapter(ProcessoImobiliarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public boolean existeEmAndamentoParaMatricula(String matricula, String uf) {
        return repositorio.contar(matricula, uf, EM_ANDAMENTO) > 0;
    }

    @Override
    public void salvar(Processo<DadosImobiliarios> processo) {
        repositorio.inserir(ProcessoImobiliarioMapper.paraEntidade(processo));
    }

    @Override
    public Optional<Processo<DadosImobiliarios>> porId(UUID id) {
        return repositorio.buscar(id).map(ProcessoImobiliarioMapper::paraDominio);
    }
}
