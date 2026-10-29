package br.com.consorcio.utilizacao.dominio.garantia.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.garantia.modelo.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class GarantiaAplicacao implements GarantiaApi {

    @Inject
    GarantiaRepository garantias;

    @Inject
    EventoGarantiaRepository eventos;

    @Override
    @Transactional
    public Long registrarFianca(String cotaRef, String cpfCnpjFiador, String nomeFiador, BigDecimal valorCobertura,
                                String chaveIdempotencia, String ator) {
        return garantias.find("chaveIdempotencia", chaveIdempotencia).firstResultOptional()
                .map(Garantia::id)
                .orElseGet(() -> criar(Garantia.fianca(cotaRef, cpfCnpjFiador, nomeFiador, valorCobertura, chaveIdempotencia), ator));
    }

    @Override
    @Transactional
    public Long proporAlienacao(PropostaAlienacao p, String ator) {
        return criar(Garantia.alienacao(p.cotaRef(), p.tipo(), p.processoRef(), p.objetoRef(), p.instrumentoRef(), p.valorCobertura()), ator);
    }

    @Override
    @Transactional
    public void registrarProtocolo(Long garantiaId, String orgao, String protocolo, String ator) {
        alterar(garantiaId, "PROTOCOLADA", ator, g -> g.protocolar(orgao, protocolo));
    }

    @Override
    @Transactional
    public void registrarRegistro(Long garantiaId, String numeroRegistro, LocalDate registradoEm, String ator) {
        alterar(garantiaId, "REGISTRADA", ator, g -> g.registrar(numeroRegistro, registradoEm));
    }

    @Override
    @Transactional
    public void conferirRegistro(Long garantiaId, String ator) {
        alterar(garantiaId, "CONFERIDA", ator, g -> g.conferir(ator));
    }

    @Override
    public Cobertura cobertura(String cotaRef, BigDecimal valorExigido) {
        BigDecimal total = garantias.list("cotaRef = ?1 and situacao = ?2", cotaRef, SituacaoGarantia.CONSTITUIDA).stream()
                .map(Garantia::valorCobertura).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Cobertura(cotaRef, valorExigido, total, total.compareTo(valorExigido) >= 0);
    }

    @Override
    public List<GarantiaResumo> doProcesso(Long processoRef) {
        return garantias.list("processoRef", processoRef).stream()
                .map(g -> new GarantiaResumo(g.id(), g.cotaRef(), g.tipo(), g.situacao().name(), g.valorCobertura()))
                .toList();
    }

    @Override
    @Transactional
    public void cancelarPropostasDoProcesso(Long processoRef, String ator) {
        garantias.list("processoRef", processoRef).forEach(g -> {
            if (g.cancelarSeNaoConstituida()) {
                eventos.persist(new EventoGarantia(g.id(), "CANCELADA", ator));
            }
        });
    }

    private Long criar(Garantia g, String ator) {
        garantias.persist(g);
        eventos.persist(new EventoGarantia(g.id(), "CRIADA", ator));
        return g.id();
    }

    private void alterar(Long id, String evento, String ator, Consumer<Garantia> acao) {
        Garantia g = garantias.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Garantia", id));
        acao.accept(g);
        eventos.persist(new EventoGarantia(g.id(), evento, ator));
    }
}
