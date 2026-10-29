package br.com.consorcio.utilizacao.dominio.nucleo.partes.api;

import java.util.Optional;

/** Pessoas e seus papéis nos processos. Outros módulos só falam com esta interface. */
public interface PartesApi {

    record PessoaResumo(Long id, String tipo, String cpfCnpj, String nome) {
    }

    record ParticipacaoResumo(Long id, Long processoId, PessoaResumo pessoa, Papel papel) {
    }

    record NovaConta(String banco, String agencia, String conta, String chavePix, boolean titularidadeVerificada) {
    }

    record ContaResumo(Long id, Long participacaoId, String banco, String agencia, String conta, String chavePix) {
    }

    PessoaResumo obterOuCriarPessoa(String tipo, String cpfCnpj, String nome);

    Optional<PessoaResumo> pessoaPorDocumento(String cpfCnpj);

    PessoaResumo pessoa(Long id);

    Long vincular(Long processoId, Long pessoaId, Papel papel);

    ParticipacaoResumo participacao(Long id);

    Long adicionarConta(Long participacaoId, NovaConta conta);

    /** A ordem só vai para conta do próprio beneficiário, com titularidade verificada. */
    ContaResumo contaVerificada(Long contaId, Long participacaoId);
}
