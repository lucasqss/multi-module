package com.example.processo.negocio.agencia;

import com.example.processo.modelo.Agencia;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.persistencia.repositorio.AgenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Cadastro de agências: o caminho curto para o que é só CRUD (ADR 0012). Um serviço com {@code @Transactional} sobre o
 * repository, sem motor nem estados. Se surgir fluxo ou variação por tipo, aí sim vira uma modalidade do processo.
 */
@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository repositorio;

    AgenciaService(AgenciaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Agencia> todas() {
        return repositorio.todas();
    }

    public Agencia porCodigo(String codigo) {
        return repositorio.buscar(codigo).orElseThrow(() -> new AgenciaNaoEncontradaException(codigo));
    }

    @Transactional
    public Agencia criar(Agencia agencia) {
        if (repositorio.buscar(agencia.getCodigo()).isPresent()) {
            throw new RegraVioladaException("Já existe agência com o código " + agencia.getCodigo());
        }
        repositorio.inserir(agencia);
        return agencia;
    }

    @Transactional
    public Agencia alterar(Agencia agencia) {
        porCodigo(agencia.getCodigo());
        repositorio.atualizar(agencia);
        return agencia;
    }

    @Transactional
    public void excluir(String codigo) {
        porCodigo(codigo);
        repositorio.excluir(codigo);
    }
}
