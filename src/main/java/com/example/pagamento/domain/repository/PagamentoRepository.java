package com.example.pagamento.domain.repository;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.exception.PagamentoNaoEncontradoException;
import com.example.pagamento.domain.exception.RecursoBloqueadoException;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.shared.Pagina;
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
 * {@code SELECT ... FOR UPDATE}. Uma segunda operação no mesmo pagamento espera até o fim da transação
 * da primeira; se o tempo de espera acabar, vira {@link RecursoBloqueadoException} (409). Leitura usa
 * {@link #obter}, sem lock. A entidade devolvida é gerenciada: alterações saem no commit.
 */
@ApplicationScoped
public class PagamentoRepository implements PanacheRepositoryBase<Pagamento, UUID> {

    /** Quanto uma operação espera pelo lock de outra antes de desistir. */
    private static final int ESPERA_LOCK_MS = 2_000;

    /** Só o pagamento NOVO precisa de persist; o carregado já é gerenciado. */
    public void salvar(Pagamento pagamento) {
        if (!isPersistent(pagamento)) {
            persist(pagamento);
        }
    }

    /** Leitura, sem lock. */
    public Pagamento obter(UUID id) {
        return findByIdOptional(id).orElseThrow(() -> new PagamentoNaoEncontradoException(id));
    }

    /** Para ALTERAR: trava a linha (FOR UPDATE) até o fim da transação. */
    public Pagamento obterParaAlteracao(UUID id) {
        try {
            return find("id", id).withLock(LockModeType.PESSIMISTIC_WRITE)
                    .withHint("jakarta.persistence.lock.timeout", ESPERA_LOCK_MS)
                    .firstResultOptional()
                    .orElseThrow(() -> new PagamentoNaoEncontradoException(id));
        } catch (PessimisticLockException | LockTimeoutException | LockAcquisitionException e) {
            throw new RecursoBloqueadoException(id); // nada de JPA vaza do repositório
        }
    }

    /** Lista paginada, mais recentes primeiro. Filtro nulo = não filtra. */
    public Pagina<Pagamento> listar(Modalidade modalidade, StatusPagamento status, int pagina, int tamanho) {
        var query = find("(:modalidade is null or modalidade = :modalidade) and (:status is null or status = :status)",
                Sort.by("criadoEm").descending().and("id"),
                Parameters.with("modalidade", modalidade).and("status", status));
        return new Pagina<>(query.page(Page.of(pagina, tamanho)).list(), pagina, tamanho, query.count());
    }

    public void remover(UUID id) {
        deleteById(id);
    }
}
