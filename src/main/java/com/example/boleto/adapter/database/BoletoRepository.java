package com.example.boleto.adapter.database;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.exception.AlteracaoConcorrenteException;
import com.example.boleto.domain.exception.BoletoNaoEncontradoException;
import com.example.boleto.domain.state.EstadoBoleto;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Documento;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.shared.Pagina;
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

/**
 * Repositório de boletos (Panache/JPA), injetado DIRETAMENTE nos use cases (sem interface).
 *
 * <p>Fala a língua do domínio: recebe e devolve {@link Boleto}, nunca {@link BoletoJpaEntity}.
 * A conversão fica no {@link BoletoMapper}, e exceções de JPA são traduzidas para exceções de domínio,
 * então nada de persistência vaza para a application.
 *
 * <p>Os métodos herdados do Panache ({@code persist}, {@code find}...) trabalham com a entidade JPA;
 * use cases devem usar só os métodos de domínio declarados aqui.
 */
@ApplicationScoped
public class BoletoRepository implements PanacheRepositoryBase<BoletoJpaEntity, UUID> {

    /** Derivado do State pattern: a regra "o que é em aberto" não é duplicada aqui. */
    private static final List<StatusBoleto> STATUS_EM_ABERTO = Arrays.stream(StatusBoleto.values())
            .filter(status -> EstadoBoleto.de(status).emAberto())
            .toList();

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

    public Optional<Boleto> buscarPorId(BoletoId id) {
        return findByIdOptional(id.valor()).map(BoletoMapper::paraDominio);
    }

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

    public long contarEmAbertoPorPagador(Documento documento) {
        return count("pagadorDocumento = ?1 and status in ?2", documento.numero(), STATUS_EM_ABERTO);
    }

    public void remover(BoletoId id) {
        deleteById(id.valor());
    }

    /** Busca pela linha digitável: é assim que o sistema externo de pagamentos identifica o boleto. */
    public Boleto obterPorLinhaDigitavel(LinhaDigitavel linhaDigitavel) {
        return find("linhaDigitavel", linhaDigitavel.valor()).firstResultOptional()
                .map(BoletoMapper::paraDominio)
                .orElseThrow(() -> new BoletoNaoEncontradoException(linhaDigitavel));
    }

    /** Atalho usado por todos os use cases que operam sobre um boleto existente. */
    public Boleto obter(BoletoId id) {
        return buscarPorId(id).orElseThrow(() -> new BoletoNaoEncontradoException(id));
    }
}
