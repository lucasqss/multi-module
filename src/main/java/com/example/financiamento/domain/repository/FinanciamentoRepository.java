package com.example.financiamento.domain.repository;

import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.exception.FinanciamentoNaoEncontradoException;
import com.example.financiamento.domain.exception.RecursoBloqueadoException;
import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import com.example.financiamento.shared.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.hibernate.exception.LockAcquisitionException;

import java.util.UUID;

/**
 * Repositório do agregado (Panache), injetado DIRETAMENTE nos casos de uso. Os métodos são INTENÇÕES de
 * negócio e já tratam o que a consulta precisa (filtros opcionais, ordenação, lock, "não encontrado"):
 * o caso de uso só chama.
 *
 * <p>CONCORRÊNCIA com lock PESSIMISTA: quem vai ALTERAR usa {@link #obterParaAlteracao}, que faz
 * {@code SELECT ... FOR UPDATE}. Uma segunda operação no mesmo financiamento espera até o fim da transação
 * da primeira; se o tempo de espera acabar, vira {@link RecursoBloqueadoException} (409). Leitura usa
 * {@link #obter}, sem lock. A entidade devolvida é gerenciada: alterações saem no commit.
 */
@ApplicationScoped
public class FinanciamentoRepository implements PanacheRepositoryBase<Financiamento, UUID> {

    /** Quanto uma operação espera pelo lock de outra antes de desistir. */
    private static final int ESPERA_LOCK_MS = 2_000;

    /** Só o financiamento NOVO precisa de persist; o carregado já é gerenciado. */
    public void salvar(Financiamento financiamento) {
        if (!isPersistent(financiamento)) {
            persist(financiamento);
        }
    }

    /** Leitura, sem lock. */
    public Financiamento obter(UUID id) {
        return findByIdOptional(id).orElseThrow(() -> new FinanciamentoNaoEncontradoException(id));
    }

    /** Para ALTERAR: trava a linha (FOR UPDATE) até o fim da transação. */
    public Financiamento obterParaAlteracao(UUID id) {
        try {
            return find("id", id).withLock(LockModeType.PESSIMISTIC_WRITE)
                    .withHint("jakarta.persistence.lock.timeout", ESPERA_LOCK_MS)
                    .firstResultOptional()
                    .orElseThrow(() -> new FinanciamentoNaoEncontradoException(id));
        } catch (PessimisticLockException | LockTimeoutException | LockAcquisitionException e) {
            throw new RecursoBloqueadoException(id); // nada de JPA vaza do repositório
        }
    }

    /** Lista paginada, mais recentes primeiro. Filtro nulo = não filtra. */
    public Pagina<Financiamento> listar(TipoFinanciamento tipo, StatusFinanciamento status, int pagina, int tamanho) {
        var query = find("(:tipo is null or tipo = :tipo) and (:status is null or status = :status)",
                Sort.by("criadoEm").descending().and("id"),
                Parameters.with("tipo", tipo).and("status", status));
        return new Pagina<>(query.page(Page.of(pagina, tamanho)).list(), pagina, tamanho, query.count());
    }

    public void remover(UUID id) {
        deleteById(id);
    }
}
