package com.example.consorcio.adapter.database;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.exception.AlteracaoConcorrenteException;
import com.example.consorcio.domain.repository.FiltroProcessoPagamento;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.state.EstadoProcessoPagamento;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import com.example.consorcio.shared.utils.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Implementação da porta {@link ProcessoPagamentoRepository} com Panache/JPA. */
@ApplicationScoped
public class ProcessoPagamentoRepositoryPanache
        implements ProcessoPagamentoRepository, PanacheRepositoryBase<ProcessoPagamentoJpaEntity, UUID> {

    /** Derivado do State pattern: a regra "o que é em andamento" não é duplicada aqui. */
    private static final List<StatusProcessoPagamento> STATUS_EM_ANDAMENTO = Arrays.stream(StatusProcessoPagamento.values())
            .filter(status -> EstadoProcessoPagamento.de(status).emAndamento())
            .toList();

    @Override
    public void salvar(ProcessoPagamento processo) {
        var existente = findById(processo.getId().valor());
        if (existente != null) {
            ProcessoPagamentoMapper.copiar(processo, existente); // entidade gerenciada: dirty checking faz o UPDATE
        } else {
            var nova = new ProcessoPagamentoJpaEntity();
            ProcessoPagamentoMapper.copiar(processo, nova);
            persist(nova);
        }
        try {
            // força o UPDATE agora (e não no commit), para o conflito de versão estourar AQUI
            // e ser traduzido para uma exceção de domínio, sem vazar JPA para as outras camadas
            flush();
        } catch (OptimisticLockException e) {
            throw new AlteracaoConcorrenteException(processo.getId());
        }
    }

    @Override
    public Optional<ProcessoPagamento> buscarPorId(ProcessoPagamentoId id) {
        return findByIdOptional(id.valor()).map(ProcessoPagamentoMapper::paraDominio);
    }

    @Override
    public Pagina<ProcessoPagamento> listar(FiltroProcessoPagamento filtro, int pagina, int tamanho) {
        var condicoes = new ArrayList<String>();
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.grupo() != null) {
            condicoes.add("grupo = :grupo");
            parametros.put("grupo", filtro.grupo());
        }
        if (filtro.cota() != null) {
            condicoes.add("cota = :cota");
            parametros.put("cota", filtro.cota());
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
                .map(ProcessoPagamentoMapper::paraDominio)
                .toList();
        return new Pagina<>(itens, pagina, tamanho, query.count());
    }

    @Override
    public boolean existeProcessoEmAndamentoParaCota(IdentificadorCota cota) {
        return count("grupo = ?1 and cota = ?2 and status in ?3",
                cota.grupo(), cota.cota(), STATUS_EM_ANDAMENTO) > 0;
    }

    @Override
    public void remover(ProcessoPagamentoId id) {
        deleteById(id.valor());
    }
}
