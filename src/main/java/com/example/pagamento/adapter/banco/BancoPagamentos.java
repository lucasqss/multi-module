package com.example.pagamento.adapter.banco;

import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.entity.TentativaPagamento;
import com.example.pagamento.domain.valueobject.Dinheiro;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Integração de SAÍDA com o BANCO: envia uma tentativa de pagamento (os boletos do processo, debitando a
 * conta informada). O resultado volta depois, pela ENTRADA {@code entrypoint.webhook.v1.BancoWebhookResource}.
 *
 * <p>SIMULAÇÃO: na vida real seria um REST client/arquivo de remessa. Aqui só registra o envio.
 */
@ApplicationScoped
public class BancoPagamentos {

    private static final Logger LOG = Logger.getLogger(BancoPagamentos.class);

    private final List<EnvioRegistrado> envios = new CopyOnWriteArrayList<>();

    public void enviar(ProcessoPagamento processo, TentativaPagamento tentativa) {
        var conta = processo.getContaDebito()
                .orElseThrow(() -> new IllegalStateException("Processo sem conta para débito: " + processo.getId()));
        envios.add(new EnvioRegistrado(tentativa.getIdTransacao(), processo.getValorTotal(), processo.getBoletos().quantidade()));
        LOG.infof("[SIMULAÇÃO] Banco: transação %s, %d boleto(s), total %s, débito ag %s cc %s",
                tentativa.getIdTransacao(), processo.getBoletos().quantidade(), processo.getValorTotal(),
                conta.agencia(), conta.conta());
    }

    public List<EnvioRegistrado> envios() {
        return List.copyOf(envios);
    }

    public record EnvioRegistrado(String idTransacao, Dinheiro valorTotal, int quantidadeBoletos) {
    }
}
