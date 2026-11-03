package com.example.consorcio.application.processopagamento;

import com.example.consorcio.application.processopagamento.dto.CriarProcessoPagamentoInput;
import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.factory.SolicitacaoPagamentoFactory;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * CREATE. O use case ORQUESTRA, não decide:
 * <ol>
 *   <li>converte a entrada em objetos de domínio;</li>
 *   <li>busca os FATOS que as regras precisam (cota, existe processo em andamento?);</li>
 *   <li>delega a DECISÃO ao domínio (specifications rodam dentro de {@code ProcessoPagamento.abrir});</li>
 *   <li>persiste e devolve a saída — tudo numa transação.</li>
 * </ol>
 * Repare que não há nenhum {@code if} de regra de negócio aqui.
 */
@ApplicationScoped
public class CriarProcessoPagamentoUC {

    private final CotaRepository cotaRepository;
    private final ProcessoPagamentoRepository processoRepository;
    private final SolicitacaoPagamentoFactory solicitacoes;

    public CriarProcessoPagamentoUC(CotaRepository cotaRepository, ProcessoPagamentoRepository processoRepository,
                                    SolicitacaoPagamentoFactory solicitacoes) {
        this.cotaRepository = cotaRepository;
        this.processoRepository = processoRepository;
        this.solicitacoes = solicitacoes;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(CriarProcessoPagamentoInput input) {
        var identificador = input.identificadorCota();
        var cota = cotaRepository.obter(identificador);
        var possuiEmAndamento = processoRepository.existeProcessoEmAndamentoParaCota(identificador);

        var processo = ProcessoPagamento.abrir(solicitacoes.paraAbertura(cota, input.dadosPagamento(), possuiEmAndamento));

        processoRepository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
