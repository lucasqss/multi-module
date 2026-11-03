package com.example.consorcio.application.pagamento;

import com.example.consorcio.application.pagamento.dto.CriarProcessoPagamentoInput;
import com.example.consorcio.application.pagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.factory.ProcessoPagamentoFactory;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Caso de uso: ORQUESTRA, não decide.
 *
 * <p>Responsabilidades daqui: converter a entrada em objetos de domínio, buscar o que é preciso
 * nas portas (repositórios), delegar as decisões ao domínio, persistir e devolver a saída,
 * tudo dentro de uma transação.
 *
 * <p>A única checagem feita aqui é a de unicidade ("já existe processo em aberto para a cota"),
 * porque ela depende de consultar o repositório, coisa que a entidade não faz.
 * Fica explícita, nomeada, e é a mesma em qualquer entrypoint.
 */
@ApplicationScoped
public class CriarProcessoPagamentoUC {

    private final CotaRepository cotaRepository;
    private final ProcessoPagamentoRepository processoRepository;
    private final ProcessoPagamentoFactory factory;

    public CriarProcessoPagamentoUC(CotaRepository cotaRepository,
                                    ProcessoPagamentoRepository processoRepository,
                                    ProcessoPagamentoFactory factory) {
        this.cotaRepository = cotaRepository;
        this.processoRepository = processoRepository;
        this.factory = factory;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(CriarProcessoPagamentoInput input) {
        // 1. Entrada -> objetos de domínio (os VOs se auto-validam)
        var identificador = new IdentificadorCota(input.grupo(), input.cota());
        var valor = new Dinheiro(input.valor());
        var contaDebito = input.agenciaDebito() == null && input.contaDebito() == null
                ? null
                : new ContaDebito(input.agenciaDebito(), input.contaDebito());

        // 2. Busca o que o domínio precisa
        Cota cota = cotaRepository.buscar(identificador)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cota", identificador));

        if (processoRepository.existeProcessoEmAbertoParaCota(identificador)) {
            throw new RegraNegocioException("PROCESSO_JA_EM_ABERTO",
                    "Já existe um processo de pagamento em aberto para a cota " + identificador);
        }

        // 3. Domínio decide (specifications + invariantes da entidade)
        ProcessoPagamento processo = factory.criar(cota, valor, input.formaPagamento(),
                contaDebito, input.dataVencimento());

        // 4. Persiste e responde
        processoRepository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
