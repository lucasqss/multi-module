package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.exception.PagamentoNaoEncontradoException;
import com.example.pagamento.domain.exception.RecursoBloqueadoException;
import com.example.pagamento.domain.valueobject.PagamentoId;
import com.example.pagamento.shared.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.hibernate.exception.LockAcquisitionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Repositório do agregado (Panache/JPA), injetado DIRETAMENTE nos use cases (sem interface).
 * Fala a língua do domínio: recebe e devolve {@link Pagamento}; nunca a entidade JPA.
 *
 * <p>CONCORRÊNCIA com lock PESSIMISTA: quem vai ALTERAR usa {@code obterParaAlteracao}, que faz
 * {@code SELECT ... FOR UPDATE}. Uma segunda operação no mesmo pagamento espera até o fim da
 * transação da primeira; se o tempo de espera acabar, vira {@link RecursoBloqueadoException} (409).
 * Leitura pura usa {@code obter}, sem lock.
 */
@ApplicationScoped
public class PagamentoRepository implements PanacheRepositoryBase<PagamentoJpaEntity, UUID> {

    /** Quanto uma operação espera pelo lock de outra antes de desistir. */
    private static final int ESPERA_LOCK_MS = 2_000;

    public void salvar(Pagamento pagamento) {
        var existente = findById(pagamento.getId().valor());
        if (existente != null) {
            PagamentoMapper.copiar(pagamento, existente); // gerenciada: o UPDATE sai no commit
        } else {
            var novo = new PagamentoJpaEntity();
            PagamentoMapper.copiar(pagamento, novo);
            persist(novo);
        }
    }

    /** Leitura, sem lock. */
    public Pagamento obter(PagamentoId id) {
        return findByIdOptional(id.valor()).map(PagamentoMapper::paraDominio)
                .orElseThrow(() -> new PagamentoNaoEncontradoException(id));
    }

    /** Para ALTERAR: trava a linha (FOR UPDATE) até o fim da transação. */
    public Pagamento obterParaAlteracao(PagamentoId id) {
        return comLock(() -> find("id", id.valor()).withLock(LockModeType.PESSIMISTIC_WRITE)
                        .withHint("jakarta.persistence.lock.timeout", ESPERA_LOCK_MS).firstResultOptional(),
                () -> new PagamentoNaoEncontradoException(id),
                () -> new RecursoBloqueadoException(id));
    }

    /** O banco identifica pelo id da transação que NÓS enviamos; também trava para alterar. */
    public Pagamento obterPorTransacaoParaAlteracao(String idTransacao) {
        return comLock(() -> find("idTransacao", idTransacao).withLock(LockModeType.PESSIMISTIC_WRITE)
                        .withHint("jakarta.persistence.lock.timeout", ESPERA_LOCK_MS).firstResultOptional(),
                () -> new PagamentoNaoEncontradoException(idTransacao),
                () -> new RecursoBloqueadoException(idTransacao));
    }

    public Pagina<Pagamento> listar(FiltroPagamento filtro, int pagina, int tamanho) {
        var condicoes = new ArrayList<String>();
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.modalidade() != null) {
            condicoes.add("modalidade = :modalidade");
            parametros.put("modalidade", filtro.modalidade());
        }
        if (filtro.status() != null) {
            condicoes.add("status = :status");
            parametros.put("status", filtro.status());
        }
        var ordenacao = Sort.by("criadoEm").descending().and("id");
        var query = condicoes.isEmpty()
                ? findAll(ordenacao)
                : find(String.join(" and ", condicoes), ordenacao, parametros);
        var itens = query.page(Page.of(pagina, tamanho)).list().stream().map(PagamentoMapper::paraDominio).toList();
        return new Pagina<>(itens, pagina, tamanho, query.count());
    }

    public void remover(PagamentoId id) {
        deleteById(id.valor());
    }

    /** Traduz as exceções de lock do JPA/Hibernate para exceção de domínio: nada de JPA vaza do adapter. */
    private static Pagamento comLock(Supplier<Optional<PagamentoJpaEntity>> consulta,
                                     Supplier<RuntimeException> naoEncontrado,
                                     Supplier<RuntimeException> bloqueado) {
        try {
            return consulta.get().map(PagamentoMapper::paraDominio).orElseThrow(naoEncontrado);
        } catch (PessimisticLockException | LockTimeoutException | LockAcquisitionException e) {
            throw bloqueado.get();
        }
    }
}
