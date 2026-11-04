package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.banco.BancoPagamentos;
import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.application.pagamento.comando.ComandoPagamento;
import com.example.pagamento.application.pagamento.comando.ContextoComando;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.valueobject.PagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * INVOCADOR do padrão Command: o único caso de uso das TRANSIÇÕES (submeter, enviar, reenviar, cancelar).
 * O roteiro é sempre o mesmo, então fica uma vez só; o que muda é o {@link ComandoPagamento}.
 *
 * <p>Usa lock PESSIMISTA: duas transições simultâneas no mesmo pagamento não se atropelam;
 * a segunda espera ou recebe 409 RECURSO_BLOQUEADO.
 */
@ApplicationScoped
public class ExecutarComandoPagamentoUC {

    private final PagamentoRepository repository;
    private final ContextoComando contexto;

    public ExecutarComandoPagamentoUC(PagamentoRepository repository, Clock clock, BancoPagamentos banco) {
        this.repository = repository;
        this.contexto = new ContextoComando(clock, banco);
    }

    @Transactional
    public PagamentoOutput executar(UUID id, ComandoPagamento comando) {
        var pagamento = repository.obterParaAlteracao(new PagamentoId(id));

        comando.executar(pagamento, contexto);

        repository.salvar(pagamento);
        comando.depoisDeSalvar(pagamento, contexto);
        return PagamentoOutput.de(pagamento);
    }
}
