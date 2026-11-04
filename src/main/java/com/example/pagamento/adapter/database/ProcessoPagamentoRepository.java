package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.exception.AlteracaoConcorrenteException;
import com.example.pagamento.domain.exception.ProcessoPagamentoNaoEncontradoException;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import com.example.pagamento.shared.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório do agregado (Panache/JPA), injetado DIRETAMENTE nos use cases (sem interface).
 * Fala a língua do domínio: recebe e devolve {@link ProcessoPagamento}; nunca a entidade JPA.
 * Não há repositório para boletos, aprovações ou tentativas: são partes do agregado.
 */
@ApplicationScoped
public class ProcessoPagamentoRepository implements PanacheRepositoryBase<ProcessoPagamentoJpaEntity, UUID> {

    public void salvar(ProcessoPagamento processo) {
        var existente = findById(processo.getId().valor());
        if (existente != null) {
            ProcessoPagamentoMapper.copiar(processo, existente);
        } else {
            var novo = new ProcessoPagamentoJpaEntity();
            ProcessoPagamentoMapper.copiar(processo, novo);
            persist(novo);
        }
        try {
            flush(); // conflito de versão estoura AQUI e vira exceção de domínio
        } catch (OptimisticLockException e) {
            throw new AlteracaoConcorrenteException(processo.getId());
        }
    }

    public Optional<ProcessoPagamento> buscarPorId(ProcessoPagamentoId id) {
        return findByIdOptional(id.valor()).map(ProcessoPagamentoMapper::paraDominio);
    }

    public ProcessoPagamento obter(ProcessoPagamentoId id) {
        return buscarPorId(id).orElseThrow(() -> new ProcessoPagamentoNaoEncontradoException(id));
    }

    /** O banco identifica pelo id da transação que NÓS enviamos. */
    public ProcessoPagamento obterPorTransacao(String idTransacao) {
        return find("select p from ProcessoPagamentoJpaEntity p join p.tentativas t where t.idTransacao = ?1", idTransacao)
                .firstResultOptional()
                .map(ProcessoPagamentoMapper::paraDominio)
                .orElseThrow(() -> new ProcessoPagamentoNaoEncontradoException(idTransacao));
    }

    /** O emissor identifica pelo id do boleto DELE; o mesmo boleto pode estar em mais de um processo. */
    public List<ProcessoPagamento> listarComBoleto(String idBoletoNoEmissor) {
        return find("select distinct p from ProcessoPagamentoJpaEntity p join p.boletos b where b.idNoEmissor = ?1",
                idBoletoNoEmissor).list().stream().map(ProcessoPagamentoMapper::paraDominio).toList();
    }

    public Pagina<ProcessoPagamento> listar(FiltroProcessoPagamento filtro, int pagina, int tamanho) {
        var condicoes = new ArrayList<String>();
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.solicitante() != null) {
            condicoes.add("solicitante = :solicitante");
            parametros.put("solicitante", filtro.solicitante().toUpperCase());
        }
        if (filtro.status() != null) {
            condicoes.add("status = :status");
            parametros.put("status", filtro.status());
        }
        var ordenacao = Sort.by("criadoEm").descending().and("id");
        var query = condicoes.isEmpty()
                ? findAll(ordenacao)
                : find(String.join(" and ", condicoes), ordenacao, parametros);
        var itens = query.page(Page.of(pagina, tamanho)).list().stream()
                .map(ProcessoPagamentoMapper::paraDominio).toList();
        return new Pagina<>(itens, pagina, tamanho, query.count());
    }

    public void remover(ProcessoPagamentoId id) {
        deleteById(id.valor());
    }
}
