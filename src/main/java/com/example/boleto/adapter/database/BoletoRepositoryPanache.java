package com.example.boleto.adapter.database;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.enums.StatusBoleto;
import com.example.boleto.domain.exception.AlteracaoConcorrenteException;
import com.example.boleto.domain.repository.BoletoRepository;
import com.example.boleto.domain.repository.FiltroBoleto;
import com.example.boleto.domain.state.EstadoBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Documento;
import com.example.boleto.shared.utils.Pagina;
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

/** Implementação da porta {@link BoletoRepository} com Panache/JPA. */
@ApplicationScoped
public class BoletoRepositoryPanache implements BoletoRepository, PanacheRepositoryBase<BoletoJpaEntity, UUID> {

    /** Derivado do State pattern: a regra "o que é em aberto" não é duplicada aqui. */
    private static final List<StatusBoleto> STATUS_EM_ABERTO = Arrays.stream(StatusBoleto.values())
            .filter(status -> EstadoBoleto.de(status).emAberto())
            .toList();

    @Override
    public void salvar(Boleto boleto) {
        var existente = findById(boleto.getId().valor());
        if (existente != null) {
            BoletoMapper.copiar(boleto, existente); // entidade gerenciada: dirty checking faz o UPDATE
        } else {
            var nova = new BoletoJpaEntity();
            BoletoMapper.copiar(boleto, nova);
            persist(nova);
        }
        try {
            // força o UPDATE agora (e não no commit), para o conflito de versão estourar AQUI
            // e ser traduzido para uma exceção de domínio, sem vazar JPA para as outras camadas
            flush();
        } catch (OptimisticLockException e) {
            throw new AlteracaoConcorrenteException(boleto.getId());
        }
    }

    @Override
    public Optional<Boleto> buscarPorId(BoletoId id) {
        return findByIdOptional(id.valor()).map(BoletoMapper::paraDominio);
    }

    @Override
    public Pagina<Boleto> listar(FiltroBoleto filtro, int pagina, int tamanho) {
        var condicoes = new ArrayList<String>();
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.documentoPagador() != null) {
            condicoes.add("pagadorDocumento = :documento");
            parametros.put("documento", filtro.documentoPagador());
        }
        if (filtro.status() != null) {
            condicoes.add("status = :status");
            parametros.put("status", filtro.status());
        }
        var ordenacao = Sort.by("emitidoEm").descending().and("id");
        var query = condicoes.isEmpty()
                ? findAll(ordenacao)
                : find(String.join(" and ", condicoes), ordenacao, parametros);

        var itens = query.page(Page.of(pagina, tamanho)).list().stream()
                .map(BoletoMapper::paraDominio)
                .toList();
        return new Pagina<>(itens, pagina, tamanho, query.count());
    }

    @Override
    public long contarEmAbertoPorPagador(Documento documento) {
        return count("pagadorDocumento = ?1 and status in ?2", documento.numero(), STATUS_EM_ABERTO);
    }

    @Override
    public void remover(BoletoId id) {
        deleteById(id.valor());
    }
}
