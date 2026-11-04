package com.example.boleto.adapter.pagamentos;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.valueobject.Pagamento;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Integração com o SISTEMA EXTERNO de pagamentos, no sentido de SAÍDA (nós chamamos ele).
 * O sentido de ENTRADA (ele nos avisa que um boleto foi pago) é um entrypoint próprio:
 * {@code entrypoint.webhook.v1.PagamentoWebhookResource}.
 *
 * <p>SIMULAÇÃO: na vida real seria um REST client com autenticação, timeout e retry; aqui só registra
 * as devoluções pedidas. Fala a língua do domínio: recebe o {@link Boleto}, não DTOs externos.
 */
@ApplicationScoped
public class SistemaPagamentos {

    private static final Logger LOG = Logger.getLogger(SistemaPagamentos.class);

    private final List<DevolucaoSolicitada> devolucoes = new CopyOnWriteArrayList<>();

    /** Pede ao sistema de pagamentos que devolva ao pagador o valor recebido. */
    public void solicitarDevolucao(Boleto boleto) {
        var pagamento = boleto.getPagamento()
                .orElseThrow(() -> new IllegalStateException("Boleto sem pagamento para devolver: " + boleto.getId()));
        devolucoes.add(new DevolucaoSolicitada(pagamento.idTransacao(), pagamento));
        LOG.infof("[SIMULAÇÃO] Devolução solicitada ao sistema de pagamentos: transação %s, valor %s",
                pagamento.idTransacao(), pagamento.valorPago());
    }

    public List<DevolucaoSolicitada> devolucoes() {
        return List.copyOf(devolucoes);
    }

    public record DevolucaoSolicitada(String idTransacao, Pagamento pagamento) {
    }
}
