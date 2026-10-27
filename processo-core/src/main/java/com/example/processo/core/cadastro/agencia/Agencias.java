package com.example.processo.core.cadastro.agencia;

import com.example.jpacore.AgenciaEntity;
import com.example.processo.core.motor.domain.exception.RegraVioladaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Cadastro de agências: o caminho curto para o que é só CRUD (ADR 0012). Um serviço com {@code @Transactional} sobre o
 * repository, sem motor, estados, ports nem lado de leitura separado. Se surgir fluxo ou variação por tipo, aí sim
 * promover para o modelo do processo.
 */
@ApplicationScoped
public class Agencias {

    private final AgenciaRepository repositorio;

    Agencias(AgenciaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Agencia> todas() {
        return repositorio.todas().stream().map(Agencias::paraDominio).toList();
    }

    public Agencia porCodigo(String codigo) {
        return repositorio.buscar(codigo).map(Agencias::paraDominio)
                .orElseThrow(() -> new AgenciaNaoEncontradaException(codigo));
    }

    @Transactional
    public Agencia criar(Agencia agencia) {
        if (repositorio.buscar(agencia.codigo()).isPresent()) {
            throw new RegraVioladaException("Já existe agência com o código " + agencia.codigo());
        }
        repositorio.inserir(paraEntidade(agencia));
        return agencia;
    }

    @Transactional
    public Agencia alterar(Agencia agencia) {
        porCodigo(agencia.codigo());
        repositorio.atualizar(paraEntidade(agencia));
        return agencia;
    }

    @Transactional
    public void excluir(String codigo) {
        porCodigo(codigo);
        repositorio.excluir(codigo);
    }

    private static Agencia paraDominio(AgenciaEntity e) {
        return new Agencia(e.getCodigo(), e.getNome(), e.getUf(), e.isAtiva());
    }

    private static AgenciaEntity paraEntidade(Agencia a) {
        return new AgenciaEntity(a.codigo(), a.nome(), a.uf(), a.ativa());
    }
}
