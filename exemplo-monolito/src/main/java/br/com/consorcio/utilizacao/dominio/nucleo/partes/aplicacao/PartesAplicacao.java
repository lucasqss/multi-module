package br.com.consorcio.utilizacao.dominio.nucleo.partes.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.Papel;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.PartesApi;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.modelo.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class PartesAplicacao implements PartesApi {

    @Inject
    PessoaRepository pessoas;

    @Inject
    ParticipacaoRepository participacoes;

    @Inject
    ContaRecebimentoRepository contas;

    @Override
    @Transactional
    public PessoaResumo obterOuCriarPessoa(String tipo, String cpfCnpj, String nome) {
        Pessoa p = pessoas.find("cpfCnpj", cpfCnpj).firstResultOptional().orElseGet(() -> {
            Pessoa nova = new Pessoa(tipo, cpfCnpj, nome);
            pessoas.persist(nova);
            return nova;
        });
        return resumo(p);
    }

    @Override
    public Optional<PessoaResumo> pessoaPorDocumento(String cpfCnpj) {
        return pessoas.find("cpfCnpj", cpfCnpj).firstResultOptional().map(PartesAplicacao::resumo);
    }

    @Override
    public PessoaResumo pessoa(Long id) {
        return resumo(pessoas.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Pessoa", id)));
    }

    @Override
    @Transactional
    public Long vincular(Long processoId, Long pessoaId, Papel papel) {
        Pessoa pessoa = pessoas.findByIdOptional(pessoaId).orElseThrow(() -> new NaoEncontradoException("Pessoa", pessoaId));
        Participacao p = new Participacao(processoId, pessoa, papel);
        participacoes.persist(p);
        return p.id();
    }

    @Override
    public ParticipacaoResumo participacao(Long id) {
        Participacao p = buscar(id);
        return new ParticipacaoResumo(p.id(), p.processoId(), resumo(p.pessoa()), p.papel());
    }

    @Override
    @Transactional
    public Long adicionarConta(Long participacaoId, NovaConta c) {
        ContaRecebimento conta = new ContaRecebimento(buscar(participacaoId), c.banco(), c.agencia(), c.conta(), c.chavePix(),
                c.titularidadeVerificada());
        contas.persist(conta);
        return conta.id();
    }

    @Override
    public ContaResumo contaVerificada(Long contaId, Long participacaoId) {
        ContaRecebimento c = contas.findByIdOptional(contaId).orElseThrow(() -> new NaoEncontradoException("Conta", contaId));
        c.exigirAptaPara(participacaoId);
        return new ContaResumo(c.id(), c.participacao().id(), c.banco(), c.agencia(), c.conta(), c.chavePix());
    }

    private Participacao buscar(Long id) {
        return participacoes.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Participação", id));
    }

    private static PessoaResumo resumo(Pessoa p) {
        return new PessoaResumo(p.id(), p.tipo(), p.cpfCnpj(), p.nome());
    }
}
